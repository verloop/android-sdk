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
}
