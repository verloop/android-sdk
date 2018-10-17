package io.verloop;

import android.app.Application;

import io.verloop.sdk.Verloop;
import io.verloop.sdk.VerloopConfig;

public class MyApp extends Application {
    private Verloop verloop;

    @Override
    public void onCreate() {
        super.onCreate();
        VerloopConfig config = new VerloopConfig("hello.stage");

//        verloop = new Verloop(getApplicationContext(), config);
    }

//    public Verloop getVerloop() {
//        return verloop;
//    }
}
