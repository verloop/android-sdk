package io.verloop;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebViewFragment;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import io.verloop.sdk.Verloop;
import io.verloop.sdk.VerloopActivity;
import io.verloop.sdk.VerloopConfig;
import io.verloop.sdk.VerloopFragment;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    VerloopFragment v;
    Verloop verloop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
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

                VerloopConfig config = new VerloopConfig("hello.stage");
                config.setFcmToken(token);
//                config.setStaging(true);
                config.putCustomField("N", "Shobhit");
                config.putCustomField("A", "26");
                config.setRecipeId("RFYuaciJDKe9rErLm");

                config.setUserEmail("anthony@gfam.com");
                config.setUserName("Anthony Gonsalves");
                config.setUserPhone("8890656400");
                verloop = new Verloop(MainActivity.this, config);

//                verloop.login("");
            }
        });
    }

    private void addVerloop() {
        verloop.showChat();
    }
}
