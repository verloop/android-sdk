package io.verloop.sdk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import io.verloop.sdk.Verloop.Companion.isActivityVisible
import io.verloop.sdk.ui.VerloopActivity
import org.json.JSONException
import org.json.JSONObject

object VerloopNotification {
    private const val TAG = "VerloopNotification"

    /**
     * Call this method in your notification listener. It checks for the `verloop` key in
     * `data` object.
     *
     *
     * Only shows notification if the user is not currently on the verloop chat screen.
     *
     * @param context   Context from service.
     * @param smallIcon Drawable integer for showing the icon in notification.
     * @param data      Data from the remote message from FCM notification. Set this value as
     * `remoteMessage.getData()`.
     * @return `true` if the notification was shown. `false` if it wasn't.
     */
    @JvmStatic
    fun showNotification(
        context: Context,
        @DrawableRes smallIcon: Int,
        data: Map<String?, String?>
    ): Boolean {
        if (data.containsKey("verloop") && !isActivityShowing(context)) {
            val json: JSONObject
            val title: String
            val text: String
            try {
                json = JSONObject(data["verloop"])
                title = json.getString("title")
                text = json.getString("text")
            } catch (e: JSONException) {
                Log.e(TAG, e.toString())
                return false
            }
            val channelId = context.packageName + ":verloop"
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(text)
                .setChannelId(channelId)

            // Create pending intent, mention the Activity which needs to be
            //triggered when user clicks on notification(StopScript.class in this case)
            val contentIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, VerloopActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
            )
            notification.setContentIntent(contentIntent)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Verloop Chat Message",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(Verloop.VERLOOP_ID, notification.build())
            return true
        }
        return false
    }

    fun cancelNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Verloop.VERLOOP_ID)
    }

    private fun isActivityShowing(context: Context): Boolean {
        return isActivityVisible
    }
}