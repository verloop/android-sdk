package io.verloop.sdk

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import io.verloop.sdk.model.LogEvent
import io.verloop.sdk.model.LogoutRequestBody
import io.verloop.sdk.service.LogoutWorker
import io.verloop.sdk.ui.VerloopActivity
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class Verloop(val context: Context, var verloopConfig: VerloopConfig) {

    fun clearChat() {
        Log.d(TAG, "clearChat() called from SDK")
        val activity = io.verloop.sdk.ui.VerloopActivity.currentInstance
        if (activity != null) {
            Log.d(TAG, "VerloopActivity.currentInstance is not null")
            val fragment = activity.supportFragmentManager.findFragmentByTag("VerloopActivity#Fragment") as? io.verloop.sdk.ui.VerloopFragment
            if (fragment != null) {
                Log.d(TAG, "VerloopFragment found, calling clearChat() on fragment")
                fragment.clearChat()
                Companion.pendingCloseChat = false
            } else {
                Log.w(TAG, "VerloopFragment not found in activity, setting pendingCloseChat = true")
                Companion.pendingCloseChat = true
            }
        } else {
            Log.w(TAG, "VerloopActivity.currentInstance is null, setting pendingCloseChat = true")
            Companion.pendingCloseChat = true
        }
    }

    val TAG = "VerloopOBJECT"
    val PREF_USER_ID = "user_id"
    private val prefName = "VerloopPreference"
    private var preference: SharedPreferences = context.getSharedPreferences(prefName, MODE_PRIVATE)

    companion object {
        const val VERLOOP_ID = 8375667

        // Global scoped state for chat activity. Used by notification handled to identify is app is
        // in foreground of not
        var isActivityVisible = false

        // Centralized access to the event listeners for all Verloop objects.
        // Used for passing callback events triggered from the WebView back to the user via ViewModel
        val eventListeners = HashMap<String?, VerloopEventListener>()

    // Global flag to track if closeChat was requested before widget was ready
    @JvmStatic
    var pendingCloseChat: Boolean = false
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
     * This will open up the activity for chat and load all the data provided in VerloopConfig
     */
    fun showChat() {
        verloopConfig.userId =
            verloopConfig.userId ?: preference.getString(PREF_USER_ID, UUID.randomUUID().toString())
        preference.edit().putString(PREF_USER_ID, verloopConfig.userId).apply()

        eventListeners[verloopConfig.hashCode().toString()] = VerloopEventListener(verloopConfig)
        val i = Intent(context, VerloopActivity::class.java)
        i.putExtra("config", verloopConfig)

        // To be used as key for eventListeners map
        i.putExtra("configKey", verloopConfig.hashCode().toString())
        context.startActivity(i)
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

        verloopConfig.userId = null
        verloopConfig.userName = null
        verloopConfig.userPhone = null
        verloopConfig.userEmail = null
        verloopConfig.fields = ArrayList()

        preference.edit().remove(PREF_USER_ID).apply()
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

        @Throws(JSONException::class)
        fun onButtonClick(json: String) {
            Log.d(TAG, " onButtonClick $json")
            val jsonObject = JSONObject(json)
            val type = jsonObject.getString("type")
            val title = jsonObject.getString("title")
            val payload = jsonObject.getString("payload")
            config.buttonOnClickListener?.buttonClicked(title, type, payload);
        }

        @Throws(JSONException::class)
        fun onURLClick(json: String) {
            Log.d(TAG, " onURLClick $json")
            val jsonObject = JSONObject(json)
            val url = jsonObject.getString("url")
            config.chatUrlClickListener?.urlClicked(url)
        }

        fun onLogEvent(logEvent: LogEvent) {
            config.logEventListener?.logEvent(logEvent)
        }
    }
}