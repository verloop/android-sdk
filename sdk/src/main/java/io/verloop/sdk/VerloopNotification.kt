package io.verloop.sdk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES.M
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import io.verloop.sdk.Verloop.Companion.isActivityVisible
import org.json.JSONException
import org.json.JSONObject

object VerloopNotification {

    private const val TAG = "VerloopNotification"

    @JvmStatic
    @Deprecated(
        "Use showNotification with channelName parameter to overwrite default channelName",
        ReplaceWith(
            "showNotification(context, smallIcon, data, channelName)",
            "io.verloop.sdk.VerloopNotification.showNotification"
        )
    )
    fun showNotification(
        context: Context,
        @DrawableRes smallIcon: Int,
        data: Map<String?, String?>,
    ): Boolean {
        return showNotification(context, smallIcon, data, "Verloop Chat Message")
    }

    /**
     * Call this method in your notification listener. It checks for the `verloop` key in
     * `data` object.
     *
     * Only shows notification if the user is not currently on the verloop chat screen.
     *
     * @param context   Context from service.
     * @param smallIcon Drawable integer for showing the icon in notification.
     * @param data      Data from the remote message from FCM notification. Set this value as
     * `remoteMessage.getData()`.
     * @param channelName Channel Name for FCM push notifications
     * @return `true` if the notification was shown. `false` if it wasn't.
     */
    @JvmStatic
    fun showNotification(
        context: Context,
        @DrawableRes smallIcon: Int,
        data: Map<String?, String?>,
        channelName: String? = "Verloop Chat Message"
    ): Boolean {
        // Show the notification only if chat activity is not running
        if (data.containsKey("verloop") && !isActivityShowing(context)) {
            val dataPayload: JSONObject
            val title: String
            val text: String
            try {
                dataPayload = JSONObject(data["verloop"])
                title = dataPayload.getString("title")
                text = dataPayload.getString("text")
            } catch (e: JSONException) {
                Log.e(TAG, e.toString())
                return false
            }
            val channelId = "verloop_chat"
            val notification =
                NotificationCompat.Builder(context, channelId).setSmallIcon(smallIcon)
                    .setContentTitle(title).setContentText(text).setChannelId(channelId)

            // Create pending intent, mention the Activity which needs to be
            //triggered when user clicks on notification(StopScript.class in this case)
            val pm: PackageManager = context.packageManager
            val notificationIntent = pm.getLaunchIntentForPackage(context.packageName)

            dataPayload.let { notificationIntent?.putExtra("verloop", it.toString()) }

            notificationIntent?.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val flag = if (Build.VERSION.SDK_INT >= M)
                (PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            else
                PendingIntent.FLAG_UPDATE_CURRENT
            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                flag
            )

            notification.setContentIntent(contentIntent)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH
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