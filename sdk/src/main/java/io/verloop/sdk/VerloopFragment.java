package io.verloop.sdk;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import static android.webkit.WebSettings.LOAD_DEFAULT;


public class VerloopFragment extends Fragment {

    private static final String TAG = "VerloopFragment";
    private WebView mWebView;

    private VerloopConfig config;
    private static final int ICE_CREAM = 12421;
    private static final int LOLLIPOP = 12422;

    private ValueCallback<Uri[]> filePathCallback;
    private ValueCallback<Uri> uploadMsg;

    public static VerloopFragment newInstance(VerloopConfig config) {
        VerloopFragment fragment = new VerloopFragment();
        Bundle args = new Bundle();
        args.putParcelable("config", config);
        fragment.setArguments(args);
        return fragment;
    }

    public void initializeWebView() {
        mWebView = new WebView(getActivity());
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // open rest of URLS in default browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                startRoom();
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

        if (getActivity().getApplicationContext().getCacheDir() != null) {
            settings.setAppCachePath(getActivity().getApplicationContext().getCacheDir().getAbsolutePath());
            settings.setAllowFileAccess(true);
            settings.setAppCacheEnabled(true);
        }

        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new VerloopInterface(this), "VerloopMobile");

        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        settings.setCacheMode(LOAD_DEFAULT);
    }

    public void loadChat() {
        // Make sure the URL is built using a library.
        Uri.Builder uriBuilder = new Uri.Builder();

        uriBuilder.scheme("https");
        if (config.isStaging()) {
            uriBuilder.authority(config.getClientId() + ".stage.verloop.io");
        } else {
            uriBuilder.authority(config.getClientId() + ".verloop.io");
        }
        uriBuilder.path("livechat");
        uriBuilder.appendQueryParameter("mode", "sdk");
        uriBuilder.appendQueryParameter("sdk", "android");
        uriBuilder.appendQueryParameter("user_id", config.getUserId());
        uriBuilder.appendQueryParameter("custom_fields", config.getFields().toString());

        if (config.getFcmToken() != null) {
            uriBuilder.appendQueryParameter("device_token", config.getFcmToken());
            uriBuilder.appendQueryParameter("device_type", "android");
        }

        if (config.getUserName() != null) {
            uriBuilder.appendQueryParameter("name", config.getUserName());
        }

        if (config.getUserEmail() != null) {
            uriBuilder.appendQueryParameter("email", config.getUserEmail());
        }

        if (config.getUserPhone() != null) {
            uriBuilder.appendQueryParameter("phone", config.getUserPhone());
        }

        if (config.getRecipeId() != null) {
            uriBuilder.appendQueryParameter("recipe_id", config.getRecipeId());
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VerloopConfig config = getArguments().getParcelable("config");
        this.config = config;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        initializeWebView();
        return mWebView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadChat();
        startRoom();
    }

    @Override
    public void onResume() {
        super.onResume();
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
        return config.getUserId() != null && config.getClientId() != null;
    }

    boolean isConfigSame(String clientId, String userId, String fcmToken, String userEmail, String userName, String userPhone, String recipeId, String customFields, boolean isStaging) {
        boolean ret = true;

        if (config.getUserId() != null)
            ret = config.getUserId().equals(userId);

//        Log.d(TAG, "Ret: "+ ret + " " + this.userId + ":" + userId);

        if (config.getClientId() != null)
            ret = ret && config.getClientId().equals(clientId);

//        Log.d(TAG, "Ret: "+ ret + " " + config.clientId + ":" + clientId);

        if (config.getFcmToken() != null)
            ret = ret && config.getFcmToken().equals(fcmToken);

//        Log.d(TAG, "Ret: "+ ret + " " + config.fcmToken + ":" + fcmToken);

        if (config.getUserName() != null)
            ret = ret && config.getUserName().equals(userName);

//        Log.d(TAG, "Ret: "+ ret + " " + config.userName + ":" + userName);

        if (config.getUserEmail() != null)
            ret = ret && config.getUserEmail().equals(userEmail);

//        Log.d(TAG, "Ret: "+ ret + " " + config.userEmail + ":" + userEmail);

        if (config.getUserPhone() != null)
            ret = ret && config.getUserPhone().equals(userPhone);

//        Log.d(TAG, "Ret: "+ ret + " " + config.userPhone + ":" + userPhone);

        if (config.getRecipeId() != null)
            ret = ret && config.getRecipeId().equals(recipeId);

        if (config.getFields() != null)
            ret = ret && config.getFields().equals(customFields);

//        Log.d(TAG, "Ret: "+ ret + " " + config.customFields + ":" + customFields);

        ret = ret && (config.isStaging() == isStaging);

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
