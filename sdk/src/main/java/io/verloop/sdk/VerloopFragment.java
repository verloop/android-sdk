package io.verloop.sdk;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;


import static android.webkit.WebSettings.LOAD_DEFAULT;


public class VerloopFragment extends Fragment {

    private static final String TAG = "VerloopFragment";
    private String title = "Chat";
    private int bgColor = Color.parseColor("#101010");
    private int textColor = Color.parseColor("#fefefe");
    private WebView mWebView;

    private String clientId, userId;

    public static VerloopFragment newInstance(Context context) {
        VerloopFragment fragment = new VerloopFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.initializeWebView(context);
        return fragment;
    }

    public void initializeWebView(Context context) {
        mWebView = new WebView(context);

        WebSettings settings = mWebView.getSettings();
        mWebView.addJavascriptInterface(new VerloopInterface(context, this), "VerloopMobile");

        if (context.getCacheDir() != null) {
            settings.setAppCachePath(context.getCacheDir().getAbsolutePath());
            settings.setAllowFileAccess(true);
            settings.setAppCacheEnabled(true);
        }

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        settings.setCacheMode(LOAD_DEFAULT);
    }

    public void loadChat(String clientId, String userId, String fcmToken) {
        this.clientId = clientId;
        this.userId = userId;
        // Make sure the URL is built using a library.
        Uri.Builder uriBuilder = new Uri.Builder();

        uriBuilder.scheme("https");
        uriBuilder.authority(this.clientId + ".verloop.io");
        uriBuilder.path("livechat");
        uriBuilder.appendQueryParameter("mode", "sdk");
        uriBuilder.appendQueryParameter("sdk", "android");
        uriBuilder.appendQueryParameter("user_id", this.userId);

        if (fcmToken != null) {
            uriBuilder.appendQueryParameter("deviceToken", fcmToken);
            uriBuilder.appendQueryParameter("deviceType", "android");
        }

        Uri uri = uriBuilder.build();

        Log.d(TAG, "URI: " + uri.toString());

        mWebView.loadUrl(uri.toString());
    }

    public void startRoom() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Log.d(TAG, "Starting Room");
            mWebView.evaluateJavascript("VerloopLivechat.start();", null);
        } else {
            mWebView.loadUrl("javascript:VerloopLivechat.start();");
        }
    }

    public void setTextColor(String textColor) {
        this.textColor = Color.parseColor(textColor);
    }

    public int getTextColor() {
        return textColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = Color.parseColor(bgColor);
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return mWebView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public void wipeData() {
        WebStorage storage = WebStorage.getInstance();
        storage.deleteAllData();
    }

    boolean isClientAndUserInitialized() {
        return this.userId != null && this.clientId != null;
    }

    boolean isClientAndUserSame(String clientId, String userId) {
        return isClientAndUserInitialized() &&
                this.userId.equals(userId) && this.clientId.equals(clientId);
    }
}
