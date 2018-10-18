package io.verloop.sdk;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;


public class VerloopActivity extends AppCompatActivity implements ServiceConnection {

    static final String TAG = "VerloopActivity";

    private VerloopService verloopService;
    private VerloopFragment verloopFragment;
    private ServiceConnection serviceConnection = this;
    private Toolbar toolbar;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUIDetails();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verloop);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(1);
        toolbar.getNavigationIcon().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);


        Intent intent = new Intent(this, VerloopService.class);

        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);


        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(getPackageName() + ".REFRESH_VERLOOP_INTERFACE");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActivityActive(true);
        VerloopNotification.cancelNotification(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setActivityActive(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addFragment() {
        if (verloopService != null) {
            Log.d(TAG, "Add Fragment from Activity");


            verloopFragment = verloopService.getFragment();

            Log.d(TAG, "Frag: " + (verloopFragment != null));

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.verloop_layout, verloopFragment, "VerloopActivity#Fragment").commit();

            // So that the keyboard doesn't cover the text input button.
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            updateUIDetails();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        VerloopService.VerloopBinder binder = (VerloopService.VerloopBinder) service;
        verloopService = binder.getService();
        addFragment();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        verloopService = null;
    }


    private void updateUIDetails() {
        if (verloopFragment != null) {
            Log.d(TAG, "Update UI from Activity");
            Log.d("verloopactivity", "DDD updateUIDetails");
            toolbar.setTitle(verloopFragment.getTitle());
            toolbar.setBackgroundColor(verloopFragment.getBgColor());
            toolbar.setTitleTextColor(verloopFragment.getTextColor());
            toolbar.getNavigationIcon().setColorFilter(verloopFragment.getTextColor(), PorterDuff.Mode.SRC_ATOP);

            verloopFragment.startRoom();
        }
    }

    private void setActivityActive(boolean isShown) {
        SharedPreferences.Editor editor = getSharedPreferences(Verloop.SHARED_PREFERENCE_FILE_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(Verloop.IS_SHOWN, isShown);
        editor.apply();
    }
}
