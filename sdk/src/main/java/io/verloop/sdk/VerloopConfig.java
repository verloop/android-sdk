package io.verloop.sdk;

import android.content.Context;
import android.content.SharedPreferences;

public class VerloopConfig {
    private String userId;
    private String clientId;
    private String fcmToken;
    private boolean isStaging;

    public VerloopConfig(String clientId) {
        this.clientId = clientId;
    }

    public VerloopConfig(String clientId, String userId) {
        this.clientId = clientId;
        this.userId = userId;
        this.isStaging = false;
    }

    String getUserId() {
        return userId;
    }

    String getClientId() {
        return clientId;
    }

    String getFcmToken() {
        return fcmToken;
    }

    boolean getStaging() {
        return isStaging;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setStaging(boolean staging) {
        this.isStaging = staging;
    }


    void save(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Verloop.CONFIG_CLIENT_ID, this.clientId);
        editor.putString(Verloop.CONFIG_USER_ID, this.userId);
        editor.putString(Verloop.CONFIG_FCM_TOKEN, this.fcmToken);
        editor.putBoolean(Verloop.CONFIG_STAGING, this.isStaging);
        editor.apply();
    }

}
