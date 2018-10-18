package io.verloop.sdk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
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

        if (!verloopFragment.isClientAndUserInitialized()) {
            Log.d(TAG, "Starting Fragment");
            SharedPreferences preferences =
                    getSharedPreferences(Verloop.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

            String clientId = preferences.getString(Verloop.CONFIG_CLIENT_ID, null);
            String userId = preferences.getString(Verloop.CONFIG_USER_ID, null);
            String fcmToken = preferences.getString(Verloop.CONFIG_FCM_TOKEN, null);

            if (clientId == null)
                throw new UnsupportedOperationException("You need to have client_id");

            if (userId == null)
                throw new UnsupportedOperationException("You need to have user_id");

            if (!getFragment().isClientAndUserSame(clientId, userId))
                getFragment().loadChat(clientId, userId, fcmToken);
            else
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
        Log.d(TAG, "onCreate");
        verloopFragment = VerloopFragment.newInstance(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getFragment().wipeData();
        verloopFragment = null;
        Log.d(TAG, "onDestroy");
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
