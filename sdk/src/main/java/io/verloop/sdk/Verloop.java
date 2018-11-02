package io.verloop.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.UUID;

public class Verloop {

    public static final String TAG = "VerloopOBJECT";

    static final int VERLOOP_ID = 8375667;

    static final String IS_SHOWN = "IS_ACTIVITY_ACTIVE";
    static final String ONLY_BIND = "ONLY_BIND";

    static final String CONFIG_CLIENT_ID = "CLIENT_ID";
    static final String CONFIG_USER_ID = "USER_ID";
    static final String CONFIG_FCM_TOKEN = "FCM_TOKEN";
    static final String CONFIG_STAGING = "IS_STAGING";

    static final String SHARED_PREFERENCE_FILE_NAME = "io.verloop.sdk";

    private Context context;

    private String userId;
    private String clientId;
    private String fcmToken;
    private boolean isStaging;


    public Verloop(Context context, VerloopConfig config) {
        this.context = context;

        this.userId = retrieveUserId(config);
        this.clientId = config.getClientId();
        this.fcmToken = config.getFcmToken();
        this.isStaging = config.getStaging();

        config.save(getPreferences());

        this.startService();
    }

    public void login(String userId) {
        login(userId, null);
    }

    public void login(String userId, String fcmToken) {
        stopService();

        this.userId = userId;
        this.fcmToken = fcmToken;

        SharedPreferences.Editor editor = getPreferences().edit();

        editor.putString(CONFIG_USER_ID, this.userId);
        editor.putString(CONFIG_FCM_TOKEN, this.fcmToken);

        editor.apply();

        startService();
    }

    public void logout() {
        stopService();

        if (fcmToken != null)
            VerloopLogoutService.logout(context, clientId, userId, fcmToken, isStaging);

        SharedPreferences.Editor editor = getPreferences().edit();

        editor.putString(CONFIG_USER_ID, null);
        editor.putString(CONFIG_FCM_TOKEN, null);

        editor.apply();
    }

    public void showChat() {
        startService();

        Intent i = new Intent(context, VerloopActivity.class);
        context.startActivity(i);
    }

    private String retrieveUserId(VerloopConfig config) {
        if (config.getUserId() == null) {
            SharedPreferences preferences = getPreferences();
            userId = preferences.getString(CONFIG_USER_ID, null);

            if (userId == null) {
                userId = UUID.randomUUID().toString();
                preferences.edit().putString(CONFIG_USER_ID, userId).apply();
            }

            config.setUserId(userId);

            return userId;
        } else
            return userId = config.getUserId();
    }

    private SharedPreferences getPreferences() {
        return context.getSharedPreferences(SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
    }

    private void startService() {
        Intent intent = new Intent(context, VerloopService.class);

        context.startService(intent);
    }

    private void stopService() {
        Intent intent = new Intent(context, VerloopService.class);
        context.stopService(intent);
    }
}
