package io.verloop.sdk

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.work.*
import io.verloop.sdk.model.LogoutRequestBody
import io.verloop.sdk.service.LogoutWorker
import io.verloop.sdk.ui.VerloopActivity
import org.json.JSONException
import org.json.JSONObject

class Verloop(val context: Context, var verloopConfig: VerloopConfig) {

    val TAG = "VerloopOBJECT"

    companion object {
        const val VERLOOP_ID = 8375667
        var isActivityVisible = false
        val eventListeners = HashMap<String?, VerloopEventListener>()
        val hideEventListeners = HashMap<String?, HideEventListener>()
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
        webView.loadUrl("https://hello.verloop.io/livechat?mode=popout")
    }

    @Deprecated("Not in use anymore")
    fun login(userId: String) {
    }

    @Deprecated("Not in use anymore")
    fun login(userId: String, fcmToken: String?) {
    }

    @Deprecated("Not in use anymore")
    fun login(verloopConfig: VerloopConfig) {
    }

    fun logout() {
        val data = Data.Builder()
            .putString(LogoutRequestBody.CLIENT_ID, verloopConfig.clientId)
            .putString(LogoutRequestBody.USER_ID, verloopConfig.userId)
            .putString(LogoutRequestBody.FCM_TOKEN, verloopConfig.fcmToken)
            .putBoolean(LogoutRequestBody.IS_STAGING, verloopConfig.isStaging)
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val logoutWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<LogoutWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()

        WorkManager
            .getInstance(context)
            .enqueue(logoutWorkRequest)

    }

    // TODO Need to use same name in JS
    fun showChat() {
        if (verloopConfig.clientId != null) eventListeners[verloopConfig.clientId] =
            VerloopEventListener(verloopConfig)
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
            config.buttonOnClickListener?.buttonClicked(title, type, payload);
        }

        @JavascriptInterface
        @Throws(JSONException::class)
        fun onURLClick(json: String) {
            Log.d(TAG, " onURLClick $json")
            val jsonObject = JSONObject(json)
            val url = jsonObject.getString("url")
            config.urlClickListener?.urlClicked(url)
        }
    }
}