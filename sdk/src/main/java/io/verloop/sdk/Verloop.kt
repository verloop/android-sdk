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

        // Global scoped state for chat activity. Used by notification handled to identify is app is
        // in foreground of not
        var isActivityVisible = false

        // Centralized access to the event listeners for all Verloop objects.
        // Used for passing callback events triggered from the WebView back to the user via ViewModel
        val eventListeners = HashMap<String?, VerloopEventListener>()
    }

    // Preload WebView with template URL to cache the contents and improve performance on next reload
    init {
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

    /**
     * This will logout the user and unregister the device from notification subscription.
     */
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

    /**
     * This will open up the activity for chat and load all the data provided in VerloopConfig
     */
    fun showChat() {
        eventListeners[verloopConfig.hashCode().toString()] = VerloopEventListener(verloopConfig)
        val i = Intent(context, VerloopActivity::class.java)
        i.putExtra("config", verloopConfig)

        // To be used as key for eventListeners map
        i.putExtra("configKey", verloopConfig.hashCode().toString())
        context.startActivity(i)
    }

    @Deprecated("Not in use anymore")
    fun hideChat() {
    }

    @Deprecated("Not in use anymore. Use Logout instead")
    fun onStopChat() {
    }

    /**
     * This class is for event listening, DO NOT use it explicitly.
     */
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
            config.chatUrlClickListener?.urlClicked(url)
        }
    }
}