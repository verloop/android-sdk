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
                verloop = new Verloop(MainActivity.this, config);
            }
        });


//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.add(R.id.fragment, VerloopFragment.newInstance(), TAG).commit();
    }

    private void addVerloop() {
//        Intent i = new Intent(this, VerloopActivity.class);
//        startActivity(i);
        verloop.showChat();
//        MyApp app = (MyApp) getApplication();
//        app.getVerloop().showChat();
    }
}
