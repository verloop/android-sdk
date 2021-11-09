package io.verloop.sdk;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class VerloopActivity extends AppCompatActivity {

    static final String TAG = "VerloopActivity";

    private VerloopFragment verloopFragment;
    private Toolbar toolbar;
    private VerloopConfig config;

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onClientInfoEvent(ClientInfoEvent event) {
        updateUIDetails();
        verloopFragment.startRoom();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verloop);
        VerloopConfig config = getIntent().getParcelableExtra("config");
        this.config = config;
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(1);
        }

        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        }

        addFragment();
    }

    /**
     * This method is for event listening, DO NOT call it explicitly.
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onHideChatEvent(HideChatEvent event) {
        onBackPressed();
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
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
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
        Log.d(TAG, "Add Fragment from Activity");
        verloopFragment = VerloopFragment.newInstance(config);
        Log.d(TAG, "Frag: " + (verloopFragment != null));

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.verloop_layout, verloopFragment, "VerloopActivity#Fragment").commit();

        // So that the keyboard doesn't cover the text input button.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        updateUIDetails();
    }

    private void updateUIDetails() {
        if (verloopFragment != null) {
            Log.d(TAG, "Update UI from Activity");
            toolbar.setTitle(verloopFragment.getTitle());
            toolbar.setBackgroundColor(verloopFragment.getBgColor());
            toolbar.setTitleTextColor(verloopFragment.getTextColor());
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setColorFilter(verloopFragment.getTextColor(), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    private void setActivityActive(boolean isShown) {
        // Set in app context
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        if (verloopFragment != null) {
            verloopFragment.fileUploadResult(requestCode, resultCode, data);
        }
    }
}
