package io.verloop.sdk;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class VerloopLogoutService extends IntentService {
    private static final String USER_ID = "io.verloop.sdk.extra.USER_ID";
    private static final String FCM_TOKEN = "io.verloop.sdk.extra.FCM_TOKEN";
    private static final String CLIENT_ID = "io.verloop.sdk.extra.CLIENT_ID";
    private static final String IS_STAGING = "io.verloop.sdk.extra.IS_STAGING";

    private static final String TAG = "VerloopLogoutService";

    public VerloopLogoutService() {
        super("VerloopLogoutService");
    }

    /**
     * Starts this service to perform action logout the user. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void logout(Context context, String clientId, String userId, String fcmToken, boolean isStaging) {
        Intent intent = new Intent(context, VerloopLogoutService.class);
        intent.putExtra(CLIENT_ID, clientId);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(FCM_TOKEN, fcmToken);
        intent.putExtra(IS_STAGING, isStaging);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String clientId = intent.getStringExtra(CLIENT_ID);
            final String userId = intent.getStringExtra(USER_ID);
            final String fcmToken = intent.getStringExtra(FCM_TOKEN);
            final boolean isStaging = intent.getBooleanExtra(IS_STAGING, false);

            try {
                Uri.Builder uriBuilder = new Uri.Builder();

                uriBuilder.scheme("https");

                if (isStaging) {
                    uriBuilder.authority(clientId + ".stage.verloop.io");
                } else {
                    uriBuilder.authority(clientId + ".verloop.io");
                }

                uriBuilder.path("api/public/sdk/unregisterDevice");

                final URL url = new URL(uriBuilder.toString());

                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();

                // setting the  Request Method Type
                httpsURLConnection.setRequestMethod("POST");
                // adding the headers for request
                httpsURLConnection.setRequestProperty("Content-Type", "application/json");
                httpsURLConnection.setRequestProperty("x-verloop-client-id", clientId);

                try {
                    httpsURLConnection.setDoOutput(true);
                    httpsURLConnection.setChunkedStreamingMode(0);

                    JSONObject obj = new JSONObject();
                    obj.put("userId", userId);
                    obj.put("deviceType", "android");
                    obj.put("deviceToken", fcmToken);

                    String json = obj.toString();

                    Log.d(TAG, "Json: " + json);

                    // to write tha data in our request
                    OutputStream outputStream = new BufferedOutputStream(httpsURLConnection.getOutputStream());
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                    outputStreamWriter.write(json);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();

                    Log.d(TAG, "Response code: " + httpsURLConnection.getResponseCode());
                    Log.d(TAG, "Response Stri: " + httpsURLConnection.getResponseMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    httpsURLConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
