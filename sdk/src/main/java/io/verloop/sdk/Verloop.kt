package io.verloop.sdk

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class Verloop(val context: Context, var verloopConfig: VerloopConfig) {

    val TAG = "VerloopOBJECT"

    companion object {
        var isActivityVisible = false
        const val VERLOOP_ID = 8375667
    }

    private lateinit var buttonOnClickListener: LiveChatButtonClickListener
    private lateinit var urlClickListener: LiveChatUrlClickListener

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

    fun showChat() {
        val i = Intent(context, VerloopActivity::class.java)
        i.putExtra("config", verloopConfig)
        context.startActivity(i)
        if ((verloopConfig.buttonOnClickListener != null || verloopConfig.urlClickListener != null) && !EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun hideChat() {
        EventBus.getDefault().post(HideChatEvent())
    }

    fun onStopChat() {
        if ((verloopConfig.buttonOnClickListener != null || verloopConfig.urlClickListener != null) && EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onChatButtonClickEvent(event: ChatButtonClickEvent) {
        if (verloopConfig.buttonOnClickListener != null) {
            val title = event.title
            val type = event.type
            val payload = event.payload
            Log.d(TAG, "Button click event received Title: $title Type: $type Payload $payload")
            verloopConfig.buttonOnClickListener!!.buttonClicked(title, type, payload)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onChatUrlClickEvent(event: ChatUrlClickEvent) {
        if (verloopConfig.urlClickListener != null) {
            val url = event.url
            Log.d(TAG, "Url click event received Url: $url")
            verloopConfig.urlClickListener!!.urlClicked(url)
        }
    }
}