package io.verloop.sdk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.IntegerRes;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;

public class VerloopNotification {


    private static final String TAG = "VerloopNotification";

    public static void showNotification(Context context, @DrawableRes int smallIcon, Map<String, String> data) {
        if (data.containsKey("verloop") && !isActivityShowing(context)) {
            JSONObject json;
            String title, text;
            try {
                json = new JSONObject(data.get("verloop"));
                title = json.getString("title");
                text = json.getString("text");
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
                return;
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
        }
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
