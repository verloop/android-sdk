package io.verloop.sdk

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import io.verloop.sdk.service.VerloopLogoutService
import io.verloop.sdk.ui.VerloopActivity
import org.json.JSONException
import org.json.JSONObject

class Verloop(val context: Context, var verloopConfig: VerloopConfig) {

    val TAG = "VerloopOBJECT"

    companion object {
        const val VERLOOP_ID = 8375667
        var isActivityVisible = false
        val eventListeners = HashMap<String, VerloopEventListener>()
        val hideEventListeners = HashMap<String, HideEventListener>()
    }

    init {
        // For Web View Performance
        val webView = WebView(context)
        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.loadUrl("https://hello.stage.verloop.io/livechat?mode=popout")
    }

    fun login(userId: String) {
        login(userId, null)
    }

    fun login(userId: String, fcmToken: String?) {
        verloopConfig.userId = userId
        verloopConfig.fcmToken = fcmToken
    }

    fun login(verloopConfig: VerloopConfig) {
        this.verloopConfig = verloopConfig
    }

    fun logout() {
        if (verloopConfig.fcmToken != null) {
            VerloopLogoutService.logout(context, verloopConfig.clientId, verloopConfig.userId, verloopConfig.fcmToken, verloopConfig.isStaging)
        }
    }

    // TODO Need to use same name in JS
    fun showChat() {
        eventListeners.put(verloopConfig.clientId, VerloopEventListener(verloopConfig))
        val i = Intent(context, VerloopActivity::class.java)
        i.putExtra("config", verloopConfig)
        context.startActivity(i)
    }

    fun hideChat() {
        hideEventListeners[verloopConfig.clientId]?.onHide()
        eventListeners.remove(verloopConfig.clientId)
        hideEventListeners.remove(verloopConfig.clientId)
    }

    fun onStopChat() {
    }

    class VerloopEventListener internal constructor(private val config: VerloopConfig) {

        companion object {
            private const val TAG = "VerloopInterface"
        }

        @JavascriptInterface
        @Throws(JSONException::class)
        fun onButtonClick(json: String) {
            Log.d(TAG, " onButtonClick $json")
            val jsonObject = JSONObject(json)
            val type = jsonObject.getString("type")
            val title = jsonObject.getString("title")
            val payload = jsonObject.getString("payload")
            if(config.buttonOnClickListener != null) {
                config.buttonOnClickListener!!.buttonClicked(title, type, payload);
            }
        }

        @JavascriptInterface
        @Throws(JSONException::class)
        fun onURLClick(json: String) {
            Log.d(TAG, " onURLClick $json")
            val jsonObject = JSONObject(json)
            val url = jsonObject.getString("url")
            if(config.urlClickListener != null) {
                config.urlClickListener!!.urlClicked(url)
            }
        }
    }
}