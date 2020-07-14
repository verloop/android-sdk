package io.verloop.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    static final String CONFIG_FIELDS = "CUSTOM_FIELDS";
    static final String CONFIG_RECIPE_ID = "RECIPE_ID";

    static final String CONFIG_USER_NAME = "USER_NAME";
    static final String CONFIG_USER_EMAIL = "USER_EMAIL";
    static final String CONFIG_USER_PHONE = "USER_PHONE";

    static final String SHARED_PREFERENCE_FILE_NAME = "io.verloop.sdk";

    private Context context;

    private String userId;
    private String clientId;
    private String fcmToken;
    private boolean isStaging;
    private LiveChatButtonClickListener buttonOnClickListener;
    private LiveChatUrlClickListener urlClickListener;

    /**
     * @param context Context of an activity/service.
     * @param config  The <code>VerloopConfig</code> object for the current user.
     */
    public Verloop(Context context, VerloopConfig config) {
        this.context = context;

        this.userId = retrieveUserId(config);
        this.clientId = config.getClientId();
        this.fcmToken = config.getFcmToken();
        this.isStaging = config.getStaging();
        this.buttonOnClickListener = config.getButtonOnClickListener();
        this.urlClickListener = config.getUrlClickListener();

        config.save(getPreferences());
        this.startService();
    }

    /**
     * @param userId User ID of the user you want to log in the app.
     * @deprecated Use {@link #login(VerloopConfig)} instead.
     * <p>
     * Login a different user than the one that this object was initialized with.
     */
    public void login(String userId) {
        login(userId, null);
    }

    /**
     * @param userId   User ID of the user you want to log in the app.
     * @param fcmToken FCM Token of the user being logged in.
     * @deprecated Use {@link #login(VerloopConfig)} instead.
     * <p>
     * Login a different user than the one that this object was initialized with.
     */
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

    /**
     * Login a different user than the one that this object was initialized with.
     *
     * @param config The <code>VerloopConfig</code> object to initialize with a new user.
     */
    public void login(VerloopConfig config) {
        this.stopService();

        this.userId = retrieveUserId(config);
        this.clientId = config.getClientId();
        this.fcmToken = config.getFcmToken();
        this.isStaging = config.getStaging();
        this.buttonOnClickListener = config.getButtonOnClickListener();
        this.urlClickListener = config.getUrlClickListener();

        config.save(getPreferences());

        this.startService();
    }

    /**
     * This will stop all the services,
     * set FCM token for user as null and
     * removes the user_id from the Shared Preferences
     */
    public void logout() {
        stopService();

        if (fcmToken != null)
            VerloopLogoutService.logout(context, clientId, userId, fcmToken, isStaging);

        SharedPreferences.Editor editor = getPreferences().edit();

        editor.putString(CONFIG_USER_ID, null);
        editor.putString(CONFIG_FCM_TOKEN, null);

        editor.apply();
    }

    /**
     * This will open up the activity for chat and load all the data provided in VerloopConfig
     */
    public void showChat() {
        startService();

        Intent i = new Intent(context, VerloopActivity.class);
        context.startActivity(i);

        if ((this.buttonOnClickListener != null || this.urlClickListener != null) && !EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void hideChat() {
        EventBus.getDefault().post(new HideChatEvent());
    }

    /**
     * This method should be called if you are listening to button clicks
     * Call this in onDestroy method of the activity
     */
    public void onStopChat() {
        if ((buttonOnClickListener != null || this.urlClickListener != null) && EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * This method is for event listening, DO NOT call it explicitly.
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onChatButtonClickEvent(ChatButtonClickEvent event) {
        if (buttonOnClickListener != null) {
            String title = event.getTitle();
            String type = event.getType();
            String payload = event.getPayload();

            Log.d(TAG, "Button click event received Title: " + title + " Type: " + type + " Payload " + payload);

            buttonOnClickListener.buttonClicked(title, type, payload);
        }
    }

    /**
     * This method is for event listening, DO NOT call it explicitly.
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onChatUrlClickEvent(ChatUrlClickEvent event) {
        if (urlClickListener != null) {
            String url = event.getUrl();

            Log.d(TAG, "Url click event received Url: " + url);

            urlClickListener.urlClicked(url);
        }
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
