package io.verloop.sdk.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.verloop.sdk.VerloopConfig
import io.verloop.sdk.api.VerloopAPI
import io.verloop.sdk.api.VerloopServiceBuilder
import io.verloop.sdk.repository.VerloopRepository
import io.verloop.sdk.viewmodel.MainViewModel
import io.verloop.sdk.viewmodel.MainViewModelFactory
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder
import java.util.*


class VerloopFragment : Fragment() {

    private var mWebView: WebView? = null
    private var config: VerloopConfig? = null
    private var configKey: String? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var uploadMsg: ValueCallback<Uri?>? = null
    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private var viewModel: MainViewModel? = null

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

    @SuppressLint("JavascriptInterface")
    fun initializeWebView() {
        mWebView = WebView(requireActivity())
        mWebView?.webViewClient = object : WebViewClient() {
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

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                startRoom()
            }
        }

        mWebView?.webChromeClient = object : WebChromeClient() {
            //Handling input[type="file"] requests for android API 16+
            fun openFileChooser(
                uploadMsg: ValueCallback<Uri?>,
                acceptType: String?,
                capture: String?
            ) {
                Log.d(TAG, "openFileChooser")
                this@VerloopFragment.uploadMsg = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "*/*"
                activity?.startActivityForResult(
                    Intent.createChooser(i, "Choose a file"),
                    ICE_CREAM
                )
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
        val settings = mWebView?.settings
        if (activity?.applicationContext?.cacheDir != null) {
            settings?.setCacheMode(WebSettings.LOAD_DEFAULT)
            settings?.allowFileAccess = true
        }
        settings?.javaScriptEnabled = true
        mWebView?.addJavascriptInterface(this, "VerloopMobile")
        settings?.domStorageEnabled = true
        settings?.allowFileAccessFromFileURLs = true
        settings?.allowUniversalAccessFromFileURLs = true
        settings?.cacheMode = WebSettings.LOAD_DEFAULT
    }

    private fun loadChat() {
        // Make sure the URL is built using a library.
        val uriBuilder = Uri.Builder()
        uriBuilder.scheme("https")
        if (config?.isStaging == true) {
            uriBuilder.authority(config?.clientId + ".stage.verloop.io")
        } else {
            uriBuilder.authority(config?.clientId + ".verloop.io")
        }
        uriBuilder.path("livechat")
        uriBuilder.appendQueryParameter("mode", "sdk")
        uriBuilder.appendQueryParameter("sdk", "android")
        uriBuilder.appendQueryParameter("user_id", config?.userId)
        if (config?.fields != null && config?.fields!!.size > 0) {
            val obj = JSONObject()
            for (field in config?.fields!!) {
                try {
                    if (field.scope != null) {
                        val innerObject = JSONObject()
                        val scopeObject = JSONObject()
                        scopeObject.put("scope", field.scope!!.name.lowercase(Locale.getDefault()))
                        innerObject.put("value", URLEncoder.encode(field.value, "utf-8"))
                        innerObject.put("options", scopeObject)
                        field.key?.let { obj.put(it, innerObject) }
                    } else {
                        field.key?.let { obj.put(it, field.value) }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            uriBuilder.appendQueryParameter("custom_fields", obj.toString())
        }
        if (config?.fcmToken != null) {
            uriBuilder.appendQueryParameter("device_token", config?.fcmToken)
            uriBuilder.appendQueryParameter("device_type", "android")
        }
        if (config?.userName != null) {
            uriBuilder.appendQueryParameter("name", config?.userName)
        }
        if (config?.userEmail != null) {
            uriBuilder.appendQueryParameter("email", config?.userEmail)
        }
        if (config?.userPhone != null) {
            uriBuilder.appendQueryParameter("phone", config?.userPhone)
        }
        if (config?.recipeId != null) {
            uriBuilder.appendQueryParameter("recipe_id", config?.recipeId)
        }
        val uri = uriBuilder.build()
        Log.d(TAG, "Verloop URI: $uri")
        mWebView?.loadUrl(uri.toString())
    }

    fun startRoom() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView?.evaluateJavascript("VerloopLivechat.start();", null)
        } else {
            mWebView?.loadUrl("javascript:VerloopLivechat.start();")
        }
    }

    fun openFileSelector() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        resultLauncher?.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = arguments?.getParcelable("config")
        configKey = arguments?.getString("configKey")

        if (config != null) {
            this.config = config
            val baseUrl =
                if (config?.isStaging === true) "https://${config?.clientId}.stage.verloop.io"
                else "https://${config?.clientId}.verloop.io"
            val retrofit =
                VerloopServiceBuilder.buildService(
                    requireContext().applicationContext,
                    baseUrl,
                    VerloopAPI::class.java
                )
            val repository = VerloopRepository(requireContext().applicationContext, retrofit)
            val viewModelFactory = MainViewModelFactory(configKey, repository)
            viewModel = activity?.let {
                ViewModelProvider(
                    it,
                    viewModelFactory
                ).get(MainViewModel::class.java)
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
                    filePathCallback = null
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        Log.d(TAG, "onCreateView")
        initializeWebView()
        return mWebView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadChat()
        startRoom()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (mWebView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) mWebView?.settings?.mediaPlaybackRequiresUserGesture =
            false
    }

    override fun onDetach() {
        super.onDetach()
        if (mWebView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) mWebView?.settings?.mediaPlaybackRequiresUserGesture =
            true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    fun fileUploadResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "Request Code: $requestCode")
        Log.d(TAG, "Result Code:  $resultCode")
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
        Log.d(TAG, " onButtonClick $json")
        viewModel?.buttonClicked(json)
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun onURLClick(json: String) {
        Log.d(TAG, " onURLClick $json")
        viewModel?.urlClicked(json)
    }
}