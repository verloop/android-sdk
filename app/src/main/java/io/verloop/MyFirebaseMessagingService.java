package io.verloop;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.verloop.sdk.VerloopNotification;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        Log.d("MyFirebaseMessagingServ", "Token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("MyFirebaseMessagingServ", remoteMessage.getFrom());

        for (String i : remoteMessage.getData().keySet()) {
            Log.d("MyFirebaseMessagingServ", "Key: " + i);
            Log.d("MyFirebaseMessagingServ", "Val: " + remoteMessage.getData().get(i));
        }

        Log.d("MyFirebaseMessagingServ", "Verloop Value: " + remoteMessage.getData().get("verloop"));

        Map<String, String> map = remoteMessage.getData();

        VerloopNotification.showNotification(this, R.mipmap.ic_launcher_round, map);
    }
}
