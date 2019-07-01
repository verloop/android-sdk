package io.verloop.sdk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class VerloopService extends Service {
    private static final String TAG = "VerloopService";


    private VerloopFragment verloopFragment;

    private IBinder binder = new VerloopBinder();

    public VerloopService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        SharedPreferences preferences =
                getSharedPreferences(Verloop.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

        String clientId = preferences.getString(Verloop.CONFIG_CLIENT_ID, null);
        String userId = preferences.getString(Verloop.CONFIG_USER_ID, null);
        String fcmToken = preferences.getString(Verloop.CONFIG_FCM_TOKEN, null);
        String userName = preferences.getString(Verloop.CONFIG_USER_NAME, null);
        String userEmail = preferences.getString(Verloop.CONFIG_USER_EMAIL, null);
        String userPhone = preferences.getString(Verloop.CONFIG_USER_PHONE, null);
        boolean isStaging = preferences.getBoolean(Verloop.CONFIG_STAGING, false);
        String recipeId = preferences.getString(Verloop.CONFIG_RECIPE_ID, null);
        String fields = preferences.getString(Verloop.CONFIG_FIELDS, null);

        if (!verloopFragment.isClientAndUserInitialized()) {
            Log.d(TAG, "Starting Fragment");

            if (clientId == null)
                throw new UnsupportedOperationException("You need to have client_id");

            if (userId == null)
                throw new UnsupportedOperationException("You need to have user_id");

//            Log.d(TAG, "clietID: " + clientId + " userID: " + userId + " isStagin: " + isStaging);

            getFragment().loadChat(clientId, userId, fcmToken, userEmail, userName, userPhone, recipeId, fields, isStaging);
        } else {
            if (!getFragment().isConfigSame(clientId, userId, fcmToken, userEmail, userName, userPhone, recipeId, fields, isStaging)) {
                Log.d(TAG, "Loading Chat.");
                getFragment().loadChat(clientId, userId, fcmToken, userEmail, userName, userPhone, recipeId, fields, isStaging);
            } else
                Log.d(TAG, "Client and User ID is same.");
        }

        return START_REDELIVER_INTENT;
    }

    public VerloopFragment getFragment() {
        return verloopFragment;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        verloopFragment = VerloopFragment.newInstance(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getFragment().wipeData();
        verloopFragment = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class VerloopBinder extends Binder {
        VerloopService getService() {
            return VerloopService.this;
        }
    }
}
