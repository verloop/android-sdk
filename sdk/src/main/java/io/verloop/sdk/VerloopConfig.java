package io.verloop.sdk;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VerloopConfig {
    private String userId;
    private String clientId;
    private String fcmToken;

    private String userName;
    private String userEmail;
    private String userPhone;

    private boolean isStaging = false;

    private ArrayList<CustomField> fields = new ArrayList<>();

    private String recipeId;

    private LiveChatButtonClickListener buttonOnClickListener;

    public enum Scope {
        USER,
        ROOM
    }

    public VerloopConfig(String clientId) {
        this.clientId = clientId;
    }

    public VerloopConfig(String clientId, String userId) {
        this.clientId = clientId;
        this.userId = userId;
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

    LiveChatButtonClickListener getButtonOnClickListener() {
        return this.buttonOnClickListener;
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

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    /**
     * Call verloop.onStopChat() on destructor if button listener is added
     * @param buttonOnClickListener
     */
    public void setButtonOnClickListener(LiveChatButtonClickListener buttonOnClickListener) {
        this.buttonOnClickListener = buttonOnClickListener;
    }

    public void putCustomField(String key, String value, Scope scope) {
        fields.add(new CustomField(key, value, scope));
    }

    public void putCustomField(String key, String value) {
        fields.add(new CustomField(key, value, null));
    }

    public void setRecipeId(String id) {
        this.recipeId = id;
    }


    void save(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Verloop.CONFIG_CLIENT_ID, this.clientId);
        editor.putString(Verloop.CONFIG_USER_ID, this.userId);
        editor.putString(Verloop.CONFIG_FCM_TOKEN, this.fcmToken);
        editor.putString(Verloop.CONFIG_USER_NAME, this.userName);
        editor.putString(Verloop.CONFIG_USER_EMAIL, this.userEmail);
        editor.putString(Verloop.CONFIG_USER_PHONE, this.userPhone);
        editor.putBoolean(Verloop.CONFIG_STAGING, this.isStaging);
        editor.putString(Verloop.CONFIG_RECIPE_ID, this.recipeId);


        JSONObject object = new JSONObject();
        for (CustomField field : fields) {
            try {
                if (field.scope != null) {
                    JSONObject innerObject = new JSONObject();
                    JSONObject scopeObject = new JSONObject();

                    scopeObject.put("scope", field.scope.name().toLowerCase());

                    innerObject.put("value", field.value);
                    innerObject.put("options", scopeObject);

                    object.put(field.key, innerObject);
                } else {
                    object.put(field.key, field.value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString(Verloop.CONFIG_FIELDS, object.toString());
        editor.apply();
    }

    class CustomField {
        String key, value;
        Scope scope;

        public CustomField(String key, String value, Scope scope) {
            this.key = key;
            this.value = value;
            this.scope = scope;
        }
    }
}
