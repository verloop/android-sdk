package io.verloop.sdk;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import static android.webkit.WebSettings.LOAD_DEFAULT;


public class VerloopFragment extends Fragment {

    private static final String TAG = "VerloopFragment";
    private String title = "Chat";
    private int bgColor = Color.parseColor("#101010");
    private int textColor = Color.parseColor("#fefefe");
    private WebView mWebView;

    private String clientId, userId, fcmToken, userName, userEmail, userPhone, customFields, recipeId;
    private boolean isStaging;

    private static final int ICE_CREAM = 12421;
    private static final int LOLLIPOP = 12422;

    private ValueCallback<Uri[]> filePathCallback;
    private ValueCallback<Uri> uploadMsg;

    public static VerloopFragment newInstance(Context context) {
        VerloopFragment fragment = new VerloopFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.initializeWebView(context);
        return fragment;
    }

    public void initializeWebView(Context context) {
        mWebView = new WebView(context);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // open rest of URLS in default browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            //Handling input[type="file"] requests for android API 16+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.d(TAG, "openFileChooser");
                VerloopFragment.this.uploadMsg = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                getActivity().startActivityForResult(Intent.createChooser(i, "Choose a file"), ICE_CREAM);
            }

            //Handling input[type="file"] requests for android API 21+
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Log.d(TAG, "onShowFileChooser");
                VerloopFragment.this.filePathCallback = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                getActivity().startActivityForResult(Intent.createChooser(i, "Choose a file"), LOLLIPOP);
                return true;
            }
        });

        WebSettings settings = mWebView.getSettings();

        if (context.getCacheDir() != null) {
            settings.setAppCachePath(context.getCacheDir().getAbsolutePath());
            settings.setAllowFileAccess(true);
            settings.setAppCacheEnabled(true);
        }

        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new VerloopInterface(context, this), "VerloopMobile");
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        settings.setCacheMode(LOAD_DEFAULT);
    }

    public void loadChat(String clientId, String userId, String fcmToken, String userEmail, String userName, String userPhone, String recipeId, String customFields, boolean isStaging) {
        this.clientId = clientId;
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userName = userName;
        this.customFields = customFields;
        this.isStaging = isStaging;
        this.recipeId = recipeId;
        // Make sure the URL is built using a library.
        Uri.Builder uriBuilder = new Uri.Builder();

        uriBuilder.scheme("https");
        if (this.isStaging) {
            uriBuilder.authority(this.clientId + ".stage.verloop.io");
        } else {
            uriBuilder.authority(this.clientId + ".verloop.io");
        }
        uriBuilder.path("livechat");
        uriBuilder.appendQueryParameter("mode", "sdk");
        uriBuilder.appendQueryParameter("sdk", "android");
        uriBuilder.appendQueryParameter("user_id", this.userId);
        uriBuilder.appendQueryParameter("custom_fields", this.customFields);

        if (this.fcmToken != null) {
            uriBuilder.appendQueryParameter("device_token", this.fcmToken);
            uriBuilder.appendQueryParameter("device_type", "android");
        }

        if (this.userName != null) {
            uriBuilder.appendQueryParameter("name", this.userName);
        }

        if (this.userEmail != null) {
            uriBuilder.appendQueryParameter("email", this.userEmail);
        }

        if (this.userPhone != null) {
            uriBuilder.appendQueryParameter("phone", this.userPhone);
        }

        if (this.recipeId != null) {
            uriBuilder.appendQueryParameter("recipe_id", this.recipeId);
        }

        Uri uri = uriBuilder.build();

        Log.d(TAG, "Verloop URI: " + uri.toString());

        mWebView.loadUrl(uri.toString());
    }

    public void startRoom() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
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

        if (mWebView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mWebView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
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

    boolean isConfigSame(String clientId, String userId, String fcmToken, String userEmail, String userName, String userPhone, String recipeId, String customFields, boolean isStaging) {
        boolean ret = true;

        if (this.userId != null)
            ret = this.userId.equals(userId);

//        Log.d(TAG, "Ret: "+ ret + " " + this.userId + ":" + userId);

        if (this.clientId != null)
            ret = ret && this.clientId.equals(clientId);

//        Log.d(TAG, "Ret: "+ ret + " " + this.clientId + ":" + clientId);

        if (this.fcmToken != null)
            ret = ret && this.fcmToken.equals(fcmToken);

//        Log.d(TAG, "Ret: "+ ret + " " + this.fcmToken + ":" + fcmToken);

        if (this.userName != null)
            ret = ret && this.userName.equals(userName);

//        Log.d(TAG, "Ret: "+ ret + " " + this.userName + ":" + userName);

        if (this.userEmail != null)
            ret = ret && this.userEmail.equals(userEmail);

//        Log.d(TAG, "Ret: "+ ret + " " + this.userEmail + ":" + userEmail);

        if (this.userPhone != null)
            ret = ret && this.userPhone.equals(userPhone);

//        Log.d(TAG, "Ret: "+ ret + " " + this.userPhone + ":" + userPhone);

        if (this.recipeId != null)
            ret = ret && this.recipeId.equals(recipeId);

        if (this.customFields != null)
            ret = ret && this.customFields.equals(customFields);

//        Log.d(TAG, "Ret: "+ ret + " " + this.customFields + ":" + customFields);

        ret = ret && (this.isStaging == isStaging);

//        Log.d(TAG, "Ret: "+ ret + " " + this.isStaging + ":" + isStaging);

        return ret;
    }

    void fileUploadResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Request Code: " + requestCode);
        Log.d(TAG, "Result Code:  " + resultCode);
        switch (requestCode) {
            case ICE_CREAM:
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }
                uploadMsg.onReceiveValue(uri);
                uploadMsg = null;
                break;
            case LOLLIPOP:
                Uri[] results = null;
                // Check that the response is a good one
                if (resultCode == Activity.RESULT_OK) {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }

                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
                break;
        }
    }
}
