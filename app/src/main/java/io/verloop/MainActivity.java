package io.verloop;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import io.verloop.sdk.VerloopConfig;
import io.verloop.sdk.Verloop;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    Verloop verloop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = findViewById(R.id.button);
        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verloop.logout();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked button");
                addVerloop();
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        Log.d(TAG, "MainActivity onCreate");

        Log.d(TAG, "token id: " + FirebaseInstanceId.getInstance().getId());
        Log.d(TAG, "token id: " + FirebaseInstanceId.getInstance().getToken());

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                String token = task.getResult().getToken();

                Log.d(TAG, "fcm token: " + token);

                VerloopConfig config = new VerloopConfig("hello");
                config.setFcmToken(token);
//                config.setStaging(true);
//                config.putCustomField("N", "Shobhit");
//                config.putCustomField("A", "26");
//                config.setRecipeId("RFYuaciJDKe9rErLm");

//                config.setUserEmail("anthony@gfam.com");
//                config.setUserName("Anthony Gonsalves");
//                config.setUserPhone("8890656400");
                config.setButtonOnClickListener((title, type, payload) -> {
                    // Add the callbacks on button clicks
                    Log.d(TAG, "title is " + title);
                });
                config.setUrlClickListener((url) -> {
                    // Add the callbacks on button clicks
                    Log.d(TAG, "url is " + url);
                });
                verloop = new Verloop(MainActivity.this, config);
            }
        });
    }

    private void addVerloop() {
        verloop.showChat();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        verloop.onStopChat();
    }
}
