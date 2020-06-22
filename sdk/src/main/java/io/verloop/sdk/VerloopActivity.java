package io.verloop.sdk;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class VerloopActivity extends AppCompatActivity implements ServiceConnection {

    static final String TAG = "VerloopActivity";

    private VerloopService verloopService;
    private boolean isServiceConnecting = false;
    private VerloopFragment verloopFragment;
    private ServiceConnection serviceConnection = this;
    private Toolbar toolbar;

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onClientInfoEvent(ClientInfoEvent event) {
        updateUIDetails();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verloop);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(1);
        }

        if(toolbar.getNavigationIcon() != null){
            toolbar.getNavigationIcon().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectWithService();

        setActivityActive(true);
        VerloopNotification.cancelNotification(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setActivityActive(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        EventBus.getDefault().unregister(this);
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
        isServiceConnecting = false;
    }


    private void updateUIDetails() {
        if (verloopFragment != null) {
            Log.d(TAG, "Update UI from Activity");
            toolbar.setTitle(verloopFragment.getTitle());
            toolbar.setBackgroundColor(verloopFragment.getBgColor());
            toolbar.setTitleTextColor(verloopFragment.getTextColor());
            if(toolbar.getNavigationIcon() != null){
                toolbar.getNavigationIcon().setColorFilter(verloopFragment.getTextColor(), PorterDuff.Mode.SRC_ATOP);
            }

            verloopFragment.startRoom();
        }
    }

    private void setActivityActive(boolean isShown) {
        SharedPreferences.Editor editor = getSharedPreferences(Verloop.SHARED_PREFERENCE_FILE_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(Verloop.IS_SHOWN, isShown);
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        verloopFragment.fileUploadResult(requestCode, resultCode, data);
    }

    private void connectWithService() {
        if (!isServiceConnecting) {
            Intent intent = new Intent(this, VerloopService.class);

            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            isServiceConnecting = true;
        }
    }
}
