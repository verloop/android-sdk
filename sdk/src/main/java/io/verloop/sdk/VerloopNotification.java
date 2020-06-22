package io.verloop.sdk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class VerloopNotification {
    private static final String TAG = "VerloopNotification";

    /**
     * Call this method in your notification listener. It checks for the <code>verloop</code> key in
     * <code>data</code> object.
     *
     * Only shows notification if the user is not currently on the verloop chat screen.
     *
     * @param context Context from service.
     * @param smallIcon Drawable integer for showing the icon in notification.
     * @param data Data from the remote message from FCM notification. Set this value as
     *             <code>remoteMessage.getData()</code>.
     *
     * @return <code>true</code> if the notification was shown. <code>false</code> if it wasn't.
     */
    public static boolean showNotification(Context context, @DrawableRes int smallIcon, Map<String, String> data) {
        if (data.containsKey("verloop") && !isActivityShowing(context)) {
            JSONObject json;
            String title, text;
            try {
                json = new JSONObject(data.get("verloop"));
                title = json.getString("title");
                text = json.getString("text");
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
                return false;
            }


            String channelId = context.getPackageName() + ":verloop";
            NotificationCompat.Builder notification =
                    new NotificationCompat.Builder(context, channelId)
                            .setSmallIcon(smallIcon)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setChannelId(channelId);

            // Create pending intent, mention the Activity which needs to be
            //triggered when user clicks on notification(StopScript.class in this case)
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, VerloopActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            notification.setContentIntent(contentIntent);




            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Verloop Chat Message", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(Verloop.VERLOOP_ID, notification.build());
            return true;
        }

        return false;
    }

    static void cancelNotification(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Verloop.VERLOOP_ID);
    }

    private static boolean isActivityShowing(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Verloop.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(Verloop.IS_SHOWN, false);
    }
}
