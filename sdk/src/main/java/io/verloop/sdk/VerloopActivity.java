package io.verloop.sdk;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.verloop_sdk.api.VerloopAPI;

import io.verloop.sdk.api.VerloopServiceBuilder;
import io.verloop.sdk.model.ClientInfo;
import io.verloop.sdk.repository.VerloopRepository;
import io.verloop.sdk.viewmodel.MainViewModel;
import io.verloop.sdk.viewmodel.MainViewModelFactory;
import retrofit2.Retrofit;

public class VerloopActivity extends AppCompatActivity {

    static final String TAG = "VerloopActivity";

    private VerloopFragment verloopFragment;
    private Toolbar toolbar;
    private VerloopConfig config;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verloop);
        VerloopConfig config = getIntent().getParcelableExtra("config");

        Retrofit retrofit = VerloopServiceBuilder.INSTANCE.buildService(getApplicationContext(), "https://hello.verloop.io/", VerloopAPI.class);
        VerloopRepository repository = new VerloopRepository(getApplicationContext(), retrofit);

        MainViewModelFactory viewModelFactory = new MainViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);

        this.config = config;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(1);
        }

        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        }

        viewModel.getClientInfo().observe(this, new Observer<ClientInfo>() {
            @Override
            public void onChanged(ClientInfo clientInfo) {
                updateClientInfo(clientInfo);
            }
        });

        Verloop.Companion.getHideEventListeners().put(config.getClientId(), new HideEventListener() {
            @Override
            public void onHide() {
                onBackPressed();
            }
        });
        addFragment();
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
        Verloop.Companion.getEventListeners().remove(config.getClientId());
        Verloop.Companion.getHideEventListeners().remove(config.getClientId());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//      switch deleted
        if (item.getItemId() == android.R.id.home) {
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
    }

    private void updateClientInfo(ClientInfo clientInfo) {
        toolbar.setTitle(clientInfo.getTitle());
        toolbar.setBackgroundColor(Color.parseColor(clientInfo.getBgColor()));
        if (clientInfo.getTextColor().length() == 4) {
            String textColor = clientInfo.getTextColor().replaceAll("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])", "#$1$1$2$2$3$3");
            clientInfo.setTextColor(textColor);
        }
        toolbar.setTitleTextColor(Color.parseColor(clientInfo.getTextColor()));
    }

    private void setActivityActive(boolean isShown) {
        Verloop.Companion.setActivityVisible(isShown);
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
