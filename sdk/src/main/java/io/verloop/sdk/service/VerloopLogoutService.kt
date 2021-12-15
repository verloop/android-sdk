package io.verloop.sdk.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import io.verloop.sdk.service.VerloopLogoutService
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class VerloopLogoutService : IntentService("VerloopLogoutService") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val clientId = intent.getStringExtra(CLIENT_ID)
            val userId = intent.getStringExtra(USER_ID)
            val fcmToken = intent.getStringExtra(FCM_TOKEN)
            val isStaging = intent.getBooleanExtra(IS_STAGING, false)
            try {
                val uriBuilder = Uri.Builder()
                uriBuilder.scheme("https")
                if (isStaging) {
                    uriBuilder.authority("$clientId.stage.verloop.io")
                } else {
                    uriBuilder.authority("$clientId.verloop.io")
                }
                uriBuilder.path("api/public/sdk/unregisterDevice")
                val url = URL(uriBuilder.toString())
                val httpsURLConnection = url.openConnection() as HttpsURLConnection

                // setting the  Request Method Type
                httpsURLConnection.requestMethod = "POST"
                // adding the headers for request
                httpsURLConnection.setRequestProperty("Content-Type", "application/json")
                httpsURLConnection.setRequestProperty("x-verloop-client-id", clientId)
                try {
                    httpsURLConnection.doOutput = true
                    httpsURLConnection.setChunkedStreamingMode(0)
                    val obj = JSONObject()
                    obj.put("userId", userId)
                    obj.put("deviceType", "android")
                    obj.put("deviceToken", fcmToken)
                    val json = obj.toString()
                    Log.d(TAG, "Json: $json")

                    // to write tha data in our request
                    val outputStream: OutputStream =
                        BufferedOutputStream(httpsURLConnection.outputStream)
                    val outputStreamWriter = OutputStreamWriter(outputStream)
                    outputStreamWriter.write(json)
                    outputStreamWriter.flush()
                    outputStreamWriter.close()
                    Log.d(TAG, "Response code: " + httpsURLConnection.responseCode)
                    Log.d(TAG, "Response Stri: " + httpsURLConnection.responseMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    httpsURLConnection.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val USER_ID = "io.verloop.sdk.extra.USER_ID"
        private const val FCM_TOKEN = "io.verloop.sdk.extra.FCM_TOKEN"
        private const val CLIENT_ID = "io.verloop.sdk.extra.CLIENT_ID"
        private const val IS_STAGING = "io.verloop.sdk.extra.IS_STAGING"
        private const val TAG = "VerloopLogoutService"

        /**
         * Starts this service to perform action logout the user. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun logout(
            context: Context,
            clientId: String?,
            userId: String?,
            fcmToken: String?,
            isStaging: Boolean
        ) {
            val intent = Intent(context, VerloopLogoutService::class.java)
            intent.putExtra(CLIENT_ID, clientId)
            intent.putExtra(USER_ID, userId)
            intent.putExtra(FCM_TOKEN, fcmToken)
            intent.putExtra(IS_STAGING, isStaging)
            context.startService(intent)
        }
    }
}