package io.verloop.sdk;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

public class VerloopInterface {

    private VerloopFragment fragment;
    private Context context;

    public static String BUTTON_TITLE = "BUTTON_TITLE";
    public static String BUTTON_TYPE = "BUTTON_TYPE";
    public static String BUTTON_PAYLOAD = "BUTTON_PAYLOAD";

    VerloopInterface(Context context, VerloopFragment fragment) {
        this.fragment = fragment;
        this.context = context;
    }

    @JavascriptInterface
    public void clientInfo(String json) throws JSONException {
        Log.d("VerloopInterface ", "DDD clientInfo " + json);
        JSONObject jsonObject = new JSONObject(json);

        String title = jsonObject.getString("title");
        String bgColor = jsonObject.getString("bgColor");
        String textColor = jsonObject.getString("textColor");

        fragment.setTitle(title);
        fragment.setBgColor(bgColor);
        fragment.setTextColor(textColor);

        String action = context.getPackageName() + ".REFRESH_VERLOOP_INTERFACE";

        Intent intent = new Intent();
        intent.setAction(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @JavascriptInterface
    public void onButtonClick(String json) throws JSONException{

        JSONObject jsonObject = new JSONObject(json);
        String title = jsonObject.getString("title");
        String type = jsonObject.getString("type");
        String payload = jsonObject.getString("payload");

        String action = context.getPackageName() + ".BUTTON_CLICK_LISTENER_VERLOOP_INTERFACE";

        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(BUTTON_TITLE, title);
        intent.putExtra(BUTTON_TYPE, type);
        intent.putExtra(BUTTON_PAYLOAD, payload);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
