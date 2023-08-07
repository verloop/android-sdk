package io.verloop.sdk.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.verloop.sdk.R
import io.verloop.sdk.VerloopConfig
import io.verloop.sdk.api.VerloopAPI
import io.verloop.sdk.api.VerloopServiceBuilder
import io.verloop.sdk.model.LogEvent
import io.verloop.sdk.model.LogLevel
import io.verloop.sdk.repository.VerloopRepository
import io.verloop.sdk.viewmodel.MainViewModel
import io.verloop.sdk.viewmodel.MainViewModelFactory
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class VerloopFragment : Fragment() {

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

        private const val JS_CALL_SET_USER_PARAMS = "SetUserParams"
        private const val JS_CALL_SET_USER_ID = "SetUserId"
        private const val JS_CALL_SET_DEPARTMENT = "SetDepartment"
        private const val JS_CALL_SET_RECIPE = "SetRecipe"
        private const val JS_CALL_SET_CUSTOM_FIELD = "SetCustomField"
        private const val JS_CALL_SET_WIDGIT_OPENED = "WidgetOpened"
        private const val JS_CALL_SET_CLOSE = "CLOSE"

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
        config = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("config", VerloopConfig::class.java)
        } else {
            @Suppress("DEPRECATION") arguments?.getParcelable("config")
        }

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
        logEvent(LogLevel.INFO, "Configuring Chat")
        mWebView.webViewClient = object : WebViewClient() {

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onReceivedHttpError(
                view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                errorResponse?.reasonPhrase?.let {
                    logEvent(LogLevel.WARNING, it)
                }
            }

            override fun onReceivedSslError(
                view: WebView?, handler: SslErrorHandler?, error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                error?.let {
                    logEvent(LogLevel.ERROR, it.toString())
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
                    logEvent(LogLevel.ERROR, it)
                }
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                error?.description?.let {
                    logEvent(LogLevel.WARNING, it.toString())
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                logEvent(LogLevel.INFO, "Page Started. Url: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                logEvent(LogLevel.INFO, "Page Finished")
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (config?.overrideUrlClick == true) {
                    return true
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return true
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest
            ): Boolean {
                val uri = request.url
                if (config?.overrideUrlClick == true) {
                    return true
                }
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
                return true
            }
        }

        mWebView.webChromeClient = object : WebChromeClient() {

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                logEvent(LogLevel.DEBUG, consoleMessage?.message().toString())
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
        Log.d(TAG, uri.toString())
        onLoadStart()
        mWebView.loadUrl(uri.toString())
    }

    private fun onLoadStart() {
        logEvent(LogLevel.INFO, "Load Chat Started")
        loading = true
        progressBar.visibility = View.VISIBLE
        mWebView.visibility = View.INVISIBLE
        layoutReload.visibility = View.GONE
        setTimeoutListener()
    }

    private fun onLoadSuccess() {
        logEvent(LogLevel.INFO, "Load Chat Successful")
        loading = false
        progressBar.visibility = View.GONE
        mWebView.visibility = View.VISIBLE
        layoutReload.visibility = View.GONE
    }

    private fun onLoadError() {
        logEvent(LogLevel.ERROR, "Load Chat Failed")
        loading = false
        progressBar.visibility = View.GONE
        mWebView.visibility = View.INVISIBLE
        layoutReload.visibility = View.VISIBLE
    }

    private fun setTimeoutListener() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (loading) {
                onLoadError()
                logEvent(LogLevel.ERROR, "Timeout. Failed to load chat. Please try again")
            }
        }, 10000)
    }

    private fun startRoom() {
        logEvent(LogLevel.INFO, "Starting room")
        config?.let { it ->
            val userParamsObject = JSONObject()
            if (!it.userEmail.isNullOrEmpty()) userParamsObject.put("email", it.userEmail)
            if (!it.userName.isNullOrEmpty()) userParamsObject.put("name", it.userName)
            if (!it.userPhone.isNullOrEmpty()) userParamsObject.put("phone", it.userPhone)

            if (userParamsObject.length() > 0) {
                logEvent(LogLevel.DEBUG, "JS_CALL: $JS_CALL_SET_USER_PARAMS: $userParamsObject")
                callJavaScript("VerloopLivechat.setUserParams(${userParamsObject});")
            }
            if (!it.userId.isNullOrEmpty()) {
                logEvent(LogLevel.DEBUG, "JS_CALL: $JS_CALL_SET_USER_ID: ${it.userId}")
                callJavaScript("VerloopLivechat.setUserId(\"${it.userId}\");")
            }
            if (!it.department.isNullOrEmpty()) {
                logEvent(LogLevel.DEBUG, "JS_CALL: $JS_CALL_SET_DEPARTMENT: ${it.department}")
                callJavaScript("VerloopLivechat.setDepartment(\"${it.department}\");")
            }
            if (!it.recipeId.isNullOrEmpty()) {
                logEvent(LogLevel.DEBUG, "JS_CALL: $JS_CALL_SET_RECIPE: ${it.recipeId}")
                callJavaScript("VerloopLivechat.setRecipe(\"${it.recipeId}\");")
            }

            // Custom Fields
            it.fields.let {
                for (field in it) {
                    val scopeObject = JSONObject()
                    if (field.scope !== null) {
                        scopeObject.put("scope", field.scope!!.name.lowercase(Locale.getDefault()))
                    }
                    logEvent(
                        LogLevel.DEBUG,
                        "JS_CALL: $JS_CALL_SET_RECIPE: key->${field.key} value->${field.value} scope->${scopeObject}"
                    )
                    callJavaScript("VerloopLivechat.setCustomField(\"${field.key}\", \"${field.value}\", ${scopeObject});")
                }
            }
        }
        logEvent(LogLevel.DEBUG, "JS_CALL: $JS_CALL_SET_WIDGIT_OPENED")
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
        logEvent(LogLevel.DEBUG, " onButtonClick $json")
        viewModel?.buttonClicked(json)
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun onURLClick(json: String) {
        logEvent(LogLevel.DEBUG, "onURLClick: $json")
        viewModel?.urlClicked(json)
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun livechatEvent(json: String) {
        logEvent(LogLevel.DEBUG, "livechatEvent: $json")
        val params = JSONObject(json)
        if (params.getString("fn").equals("ready")) {
            ready()
        } else if (params.getString("fn").equals("roomReady")) {
            roomReady()
        }
    }

    private fun ready() {
        logEvent(LogLevel.INFO, "Ready")
        Handler(Looper.getMainLooper()).post {
            startRoom()
        }
    }

    private fun roomReady() {
        logEvent(LogLevel.INFO, "Room Ready")
        Handler(Looper.getMainLooper()).post {
            onLoadSuccess()
            if (config?.closeExistingChat == true) {
                logEvent(LogLevel.DEBUG, "JS_CALL: $JS_CALL_SET_CLOSE")
                callJavaScript("VerloopLivechat.close();")
            }
        }
    }


    private fun logEvent(level: LogLevel, message: String) {
        if (config?.logLevel?.ordinal!! >= level.ordinal) {
            viewModel?.logEvent(LogEvent(level.name, message))
        }
    }
}