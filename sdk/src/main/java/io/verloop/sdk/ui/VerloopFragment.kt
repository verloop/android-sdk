package io.verloop.sdk.ui

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.verloop.sdk.Constants
import io.verloop.sdk.R
import io.verloop.sdk.VerloopConfig
import io.verloop.sdk.api.VerloopAPI
import io.verloop.sdk.api.VerloopServiceBuilder
import io.verloop.sdk.model.LogEvent
import io.verloop.sdk.model.LogLevel
import io.verloop.sdk.repository.VerloopRepository
import io.verloop.sdk.utils.CommonUtils
import io.verloop.sdk.viewmodel.MainViewModel
import io.verloop.sdk.viewmodel.MainViewModelFactory
import org.json.JSONException
import org.json.JSONObject
import androidx.annotation.RequiresApi
import java.util.*

class VerloopFragment : Fragment() {
    /**
     * Call this method from outside to close the chat widget.
     * If the widget is ready, it will call verloopLivechat.close() immediately.
     * Otherwise, it will store the request and call it after roomReady.
     */
    fun clearChat() {
        Log.d(TAG, "clearChat() called in VerloopFragment")
        val ready = isWidgetReady()
        Log.d(TAG, "isWidgetReady() = $ready, loading = $loading, webViewVisible = ${if (::mWebView.isInitialized) mWebView.visibility == View.VISIBLE else "not initialized"}")
        if (ready) {
            Log.d(TAG, "Widget is ready, calling VerloopLivechat.close() via JS")
            callJavaScript("VerloopLivechat.close();")
            io.verloop.sdk.Verloop.pendingCloseChat = false
        } else {
            Log.d(TAG, "Widget not ready, setting pendingCloseChat = true")
            io.verloop.sdk.Verloop.pendingCloseChat = true
        }
    }

    // Helper to check if widget is ready (after roomReady)
    private fun isWidgetReady(): Boolean {
        // You can improve this logic if you have a more robust ready state
        return mWebView.visibility == View.VISIBLE && !loading
    }

    private lateinit var baseUri: String
    private lateinit var mWebView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutReload: LinearLayout
    private lateinit var buttonReload: Button

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var uploadMsg: ValueCallback<Uri?>? = null
    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    private var viewModel: MainViewModel? = null
    private var configKey: String? = null
    private var config: VerloopConfig? = null
    private var loading: Boolean = false

    companion object {
        private const val TAG = "VerloopFragment"
        private const val ICE_CREAM = 12421
        private const val LOLLIPOP = 12422

        fun newInstance(configKey: String?, config: VerloopConfig?): VerloopFragment {
            val fragment = VerloopFragment()
            val args = Bundle()
            args.putParcelable("config", config)
            args.putString("configKey", configKey)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = arguments?.let { BundleCompat.getParcelable(it, "config",  VerloopConfig::class.java) }
        configKey = arguments?.getString("configKey")

        if (config != null) {
            val baseUrl =
                if (config?.isStaging == true) "https://${config?.clientId}.stage.verloop.io"
                else "https://${config?.clientId}.verloop.io"
            val retrofit = VerloopServiceBuilder.buildService(
                requireContext().applicationContext, baseUrl, VerloopAPI::class.java
            )
            val repository = VerloopRepository(requireContext().applicationContext, retrofit)
            val viewModelFactory = MainViewModelFactory(configKey, repository)
            viewModel = activity?.let {
                ViewModelProvider(
                    it, viewModelFactory
                )[MainViewModel::class.java]
            }
        }

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    var results: Array<Uri>? = null
                    // Check that the response is a good one
                    val dataString = data?.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                    filePathCallback?.onReceiveValue(results)
                } else {
                    filePathCallback?.onReceiveValue(null)
                }
                filePathCallback = null
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verloop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isKeyboardVisible = ime.bottom > 0
            // on opening of keyboard ime.bottom also includes systemBars.bottom, this creates extra padding beneath inputbox, so subtracting
            val bottomInset = if (isKeyboardVisible) ime.bottom - systemBars.bottom else 0
            v.setPadding(systemBars.left, 0, systemBars.right, bottomInset)
            WindowInsetsCompat.CONSUMED
        }

        progressBar = view.findViewById(R.id.progressBar)
        mWebView = view.findViewById(R.id.webView)
        layoutReload = view.findViewById(R.id.layoutReload)
        buttonReload = view.findViewById(R.id.buttonReload)

        buttonReload.setOnClickListener {
            this.mWebView.removeAllViews()
            initializeWebView()
            loadChat()
        }

        initializeWebView()
        loadChat()
    }

    override fun onDestroyView() {
        this.mWebView.removeAllViews()
        super.onDestroyView()
    }

    override fun onDestroy() {
        this.mWebView.destroy()
        super.onDestroy()
    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    fun initializeWebView() {
        logEvent(LogLevel.INFO, "Configuring Chat", null)
        mWebView.webViewClient = object : WebViewClient() {

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onReceivedHttpError(
                view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                errorResponse?.reasonPhrase?.let {
                    logEvent(LogLevel.WARNING, it, null)
                }
            }

            override fun onReceivedSslError(
                view: WebView?, handler: SslErrorHandler?, error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                error?.let {
                    logEvent(LogLevel.ERROR, it.toString(), null)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView?, errorCode: Int, description: String?, failingUrl: String?
            ) {
                @Suppress("DEPRECATION")
                super.onReceivedError(
                    view,
                    errorCode,
                    description,
                    failingUrl
                )
                description?.let {
                    logEvent(LogLevel.ERROR, it, null)
                }
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                error?.description?.let {
                    logEvent(LogLevel.WARNING, it.toString(), null)
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                val params = JSONObject().put("url", url)
                logEvent(LogLevel.INFO, "Page Started", params)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                logEvent(LogLevel.INFO, "Page Finished", null)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith(baseUri)) {
                    return false
                }
                return true
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest
            ): Boolean {
                var url: String = request.url.toString()
                if (url.startsWith(baseUri)) {
                    return false
                }
                return true
            }
        }

        mWebView.webChromeClient = object : WebChromeClient() {

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                logEvent(LogLevel.DEBUG, consoleMessage?.message().toString(), null)
                return super.onConsoleMessage(consoleMessage)
            }

            //Handling input[type="file"] requests for android API 21+
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                Log.d(TAG, "onShowFileChooser")
                this@VerloopFragment.filePathCallback = filePathCallback
                openFileSelector()
                return true
            }
        }

        val settings: WebSettings = mWebView.settings
        if (activity?.applicationContext?.cacheDir != null) {
            settings.allowFileAccess = true
        }
        settings.javaScriptEnabled = true
        mWebView.addJavascriptInterface(this, "VerloopMobile")
        mWebView.addJavascriptInterface(this, "VerloopMobileV2")
        settings.domStorageEnabled = true
        @Suppress("DEPRECATION") settings.allowFileAccessFromFileURLs = true
        @Suppress("DEPRECATION") settings.allowUniversalAccessFromFileURLs = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.mediaPlaybackRequiresUserGesture = false
    }

    private fun loadChat() {
        // Make sure the URL is built using a library.
        val uriBuilder = Uri.Builder()
        uriBuilder.scheme("https")
        uriBuilder.authority(config?.clientId + ".verloop.io")
        uriBuilder.path("livechat")
        uriBuilder.appendQueryParameter("mode", "sdk")
        uriBuilder.appendQueryParameter("sdk", "android")
        if (config?.fcmToken != null) {
            uriBuilder.appendQueryParameter("device_token", config?.fcmToken)
            uriBuilder.appendQueryParameter("device_type", "android")
        }
        val uri = uriBuilder.build()
        baseUri = "${uri.scheme}://${uri.authority}${uri.path}"
        Log.d(TAG, uri.toString())
        onLoadStart()
        mWebView.loadUrl(uri.toString())
    }

    private fun onLoadStart() {
        logEvent(LogLevel.INFO, "Load Chat Started", null)
        loading = true
        progressBar.visibility = View.VISIBLE
        mWebView.visibility = View.INVISIBLE
        layoutReload.visibility = View.GONE
        setTimeoutListener()
    }

    private fun onLoadSuccess() {
        logEvent(LogLevel.INFO, "Load Chat Successful", null)
        loading = false
        progressBar.visibility = View.GONE
        mWebView.visibility = View.VISIBLE
        layoutReload.visibility = View.GONE
    }

    private fun onLoadError() {
        logEvent(LogLevel.ERROR, "Load Chat Failed", null)
        loading = false
        progressBar.visibility = View.GONE
        mWebView.visibility = View.INVISIBLE
        layoutReload.visibility = View.VISIBLE
    }

    private fun setTimeoutListener() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (loading) {
                onLoadError()
                logEvent(LogLevel.ERROR, "Timeout. Failed to load chat. Please try again", null)
            }
        }, 10000)
    }

    private fun startRoom() {
        logEvent(LogLevel.INFO, "Starting room", null)
        config?.let { it ->
            val userParamsObject = JSONObject()
            if (!it.userEmail.isNullOrEmpty()) userParamsObject.put("email", it.userEmail)
            if (!it.userName.isNullOrEmpty()) userParamsObject.put("name", it.userName)
            if (!it.userPhone.isNullOrEmpty()) userParamsObject.put("phone", it.userPhone)

            if (userParamsObject.length() > 0) {
                logEvent(LogLevel.DEBUG, Constants.JS_CALL_SET_USER_PARAMS, userParamsObject)
                callJavaScript("VerloopLivechat.setUserParams(${userParamsObject});")
            }
            if (!it.userId.isNullOrEmpty()) {
                logEvent(
                    LogLevel.DEBUG,
                    Constants.JS_CALL_SET_USER_ID,
                    JSONObject().put("userId", it.userId)
                )
                callJavaScript("VerloopLivechat.setUserId(\"${it.userId}\");")
            }
            if (!it.department.isNullOrEmpty()) {
                logEvent(
                    LogLevel.DEBUG, Constants.JS_CALL_SET_DEPARTMENT,
                    JSONObject().put("department", it.department)
                )
                callJavaScript("VerloopLivechat.setDepartment(\"${it.department}\");")
            }
            if (!it.recipeId.isNullOrEmpty()) {
                logEvent(
                    LogLevel.DEBUG, Constants.JS_CALL_SET_RECIPE,
                    JSONObject().put("recipeId", it.recipeId)
                )
                callJavaScript("VerloopLivechat.setRecipe(\"${it.recipeId}\");")
            }

            // Custom Fields
            it.fields.let {
                for (field in it) {
                    val scopeObject = JSONObject()
                    if (field.scope !== null) {
                        scopeObject.put("scope", field.scope!!.name.lowercase(Locale.getDefault()))
                    }
                    val params = JSONObject()
                    params.put("key", field.key)
                    params.put("value", field.value)
                    params.put("scope", field.scope)
                    logEvent(
                        LogLevel.DEBUG,
                        Constants.JS_CALL_SET_CUSTOM_FIELD,
                        params
                    )
                    callJavaScript("VerloopLivechat.setCustomField(\"${field.key}\", \"${field.value}\", ${scopeObject});")
                }
            }

            if (it.allowFileDownload) {
                logEvent(
                    LogLevel.DEBUG, Constants.JS_CALL_SHOW_DOWNLOAD_BUTTON,
                    JSONObject().put("showDownloadButton", it.allowFileDownload)
                )
                callJavaScript("VerloopLivechat.showDownloadButton(\"${it.allowFileDownload}\");")
            }
        }
        logEvent(LogLevel.DEBUG, Constants.JS_CALL_WIDGET_OPENED, null)
        callJavaScript("VerloopLivechat.widgetOpened();")
    }

    private fun callJavaScript(script: String) {
        mWebView.evaluateJavascript(script, null)
    }

    fun openFileSelector() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        resultLauncher?.launch(intent)
    }

    fun fileUploadResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ICE_CREAM -> {
                var uri: Uri? = null
                if (data != null) {
                    uri = data.data
                }
                uploadMsg?.onReceiveValue(uri)
                uploadMsg = null
            }
            LOLLIPOP -> {
                var results: Array<Uri>? = null
                // Check that the response is a good one
                if (resultCode == Activity.RESULT_OK) {
                    val dataString = data?.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
                filePathCallback?.onReceiveValue(results)
                filePathCallback = null
            }
        }
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun onButtonClick(json: String) {
        logEvent(LogLevel.DEBUG, "onButtonClick", JSONObject(json))
        viewModel?.buttonClicked(json)
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun onURLClick(json: String) {
        if (config?.overrideUrlClick == false) {
            val jsonObject = JSONObject(json)
            val url = jsonObject.getString("url");
            if (!url.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
        logEvent(LogLevel.DEBUG, "onURLClick", JSONObject(json))
        viewModel?.urlClicked(json)
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun onDownloadClick(jsonString: String?) {
        if (!jsonString.isNullOrEmpty()) {
            val jsonObject = JSONObject(jsonString)
            val url = jsonObject.getString("url")
            downloadFile(url)
        }
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun livechatEvent(json: String) {
        logEvent(LogLevel.DEBUG, "livechatEvent", JSONObject(json))
        val params = JSONObject(json)
        if (params.getString("fn").equals("ready")) {
            ready()
        } else if (params.getString("fn").equals("roomReady")) {
            roomReady()
        }
    }

    private fun ready() {
        logEvent(LogLevel.INFO, "Ready", null)
        Handler(Looper.getMainLooper()).post {
            startRoom()
        }
    }

    private fun roomReady() {
        logEvent(LogLevel.INFO, "Room Ready", null)
        Handler(Looper.getMainLooper()).post {
            onLoadSuccess()
            if (config?.closeExistingChat == true) {
                logEvent(LogLevel.DEBUG, Constants.JS_CALL_CLOSE, null)
                callJavaScript("VerloopLivechat.close();")
            }
            // New: If clearChat was requested before ready, call it now
            if (io.verloop.sdk.Verloop.pendingCloseChat) {
                logEvent(LogLevel.DEBUG, "External clearChat invoked after ready", null)
                callJavaScript("VerloopLivechat.close();")
                io.verloop.sdk.Verloop.pendingCloseChat = false
            }
            if (config?.openMenuWidgetOnStart == true) {
                logEvent(LogLevel.DEBUG, Constants.JS_CALL_OPEN_MENU_WIDGET, null)
                callJavaScript("VerloopLivechat.openMenuWidget();")
            }
        }
    }

    private fun logEvent(level: LogLevel, message: String, params: JSONObject?) {
        if (config?.logLevel?.ordinal!! >= level.ordinal) {
            viewModel?.logEvent(LogEvent(level.name, message, params))
        }
    }

    private fun downloadFile(url: String?) {
        if (url.isNullOrEmpty())
            return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }

        startDownloadRequest(url)
    }

    private fun startDownloadRequest(url: String){
        showToast(getString(R.string.download_started))
        val (fileNameFull, extension) = CommonUtils.getFileNameAndExtension(url)

        val downloadManager =
            requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileNameFull
            )
            .setTitle("Downloading $extension File")
            .setDescription("Downloading $fileNameFull")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        downloadManager.enqueue(request)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showToast("Permission Granted, Try Again!")
        } else {
            showToast("Need to grant Permission to continue download!")
        }
    }

    private fun showToast(msg: String){
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}