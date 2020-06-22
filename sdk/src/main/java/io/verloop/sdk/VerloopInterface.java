package io.verloop.sdk;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

public class VerloopInterface {

    private static final String TAG = "VerloopInterface";

    private VerloopFragment fragment;

    VerloopInterface(VerloopFragment fragment) {
        this.fragment = fragment;
    }

    @JavascriptInterface
    public void clientInfo(String json) throws JSONException {
        Log.d(TAG, "DDD clientInfo " + json);
        JSONObject jsonObject = new JSONObject(json);

        String title = jsonObject.getString("title");
        String bgColor = jsonObject.getString("bgColor");
        String textColor = jsonObject.getString("textColor");

        fragment.setTitle(title);
        fragment.setBgColor(bgColor);
        fragment.setTextColor(textColor);

        EventBus.getDefault().postSticky(new ClientInfoEvent(title, bgColor, textColor));
    }

    @JavascriptInterface
    public void onButtonClick(String json) throws JSONException {
        Log.d(TAG, " onButtonClick " + json);

        JSONObject jsonObject = new JSONObject(json);

        String type = jsonObject.getString("type");
        String title = jsonObject.getString("title");
        String payload = jsonObject.getString("payload");

        EventBus.getDefault().postSticky(new ChatButtonClickEvent(type, title, payload));
    }
}
