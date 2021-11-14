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
    public void onButtonClick(String json) throws JSONException {
        Log.d(TAG, " onButtonClick " + json);

        JSONObject jsonObject = new JSONObject(json);

        String type = jsonObject.getString("type");
        String title = jsonObject.getString("title");
        String payload = jsonObject.getString("payload");

        EventBus.getDefault().post(new ChatButtonClickEvent(type, title, payload));
    }

    @JavascriptInterface
    public void onURLClick(String json) throws JSONException {
        Log.d(TAG, " onURLClick " + json);

        JSONObject jsonObject = new JSONObject(json);

        String url = jsonObject.getString("url");

        EventBus.getDefault().post(new ChatUrlClickEvent(url));
    }
}
