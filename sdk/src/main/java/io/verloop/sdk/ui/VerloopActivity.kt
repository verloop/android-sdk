package io.verloop.sdk.ui

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.ViewModelProvider
import io.verloop.sdk.R
import io.verloop.sdk.Verloop.Companion.eventListeners
import io.verloop.sdk.Verloop.Companion.isActivityVisible
import io.verloop.sdk.VerloopConfig
import io.verloop.sdk.VerloopNotification
import io.verloop.sdk.api.VerloopAPI
import io.verloop.sdk.api.VerloopServiceBuilder.buildService
import io.verloop.sdk.model.ClientInfo
import io.verloop.sdk.model.LogEvent
import io.verloop.sdk.model.LogLevel
import io.verloop.sdk.repository.VerloopRepository
import io.verloop.sdk.utils.CommonUtils
import io.verloop.sdk.viewmodel.MainViewModel
import io.verloop.sdk.viewmodel.MainViewModelFactory
import org.json.JSONObject

class VerloopActivity : AppCompatActivity() {

    private lateinit var verloopFragment: VerloopFragment
    private var toolbar: Toolbar? = null
    private var config: VerloopConfig? = null
    private var viewModel: MainViewModel? = null
    private var configKey: String? = null

    companion object {
        const val TAG = "VerloopActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verloop)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 1f
        toolbar?.navigationIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                ContextCompat.getColor(applicationContext, R.color.white),
                BlendModeCompat.SRC_ATOP
            )

        val config: VerloopConfig? = intent.getParcelableExtra("config")
        configKey = intent.getStringExtra("configKey")
        this.config = config

        if (config != null) {
            logEvent(LogLevel.DEBUG, "$TAG:onCreate", null)
            val baseUrl =
                if (config.isStaging) "https://${config.clientId}.stage.verloop.io"
                else "https://${config.clientId}.verloop.io"

            val retrofit =
                buildService(
                    applicationContext,
                    baseUrl,
                    VerloopAPI::class.java
                )
            val repository = VerloopRepository(applicationContext, retrofit)
            val viewModelFactory = MainViewModelFactory(configKey, repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
            viewModel?.getClientInfo()?.observe(this) { clientInfo -> updateClientInfo(clientInfo) }
            addFragment()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onResume() {
        super.onResume()
        setActivityActive(true)
        VerloopNotification.cancelNotification(this)
    }

    override fun onPause() {
        super.onPause()
        setActivityActive(false)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        logEvent(LogLevel.DEBUG, "$TAG:onDestroy", null)
        super.onDestroy()
        eventListeners.remove(configKey)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Respond to the action bar's Up/Home button
            finish()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addFragment() {
        Log.d(TAG, "addFragment")
        logEvent(LogLevel.DEBUG, "$TAG:addFragment", null)
        verloopFragment = VerloopFragment.newInstance(configKey, config)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.verloop_layout, verloopFragment, "VerloopActivity#Fragment").commit()
    }

    private fun updateClientInfo(clientInfo: ClientInfo) {
        Log.d(TAG, "updateClientInfo")
        logEvent(LogLevel.DEBUG, "$TAG:updateClientInfo", null)
        toolbar?.title = clientInfo.title
        toolbar?.setBackgroundColor(Color.parseColor(clientInfo.bgColor ?: "#FFFFFF"))
        toolbar?.setTitleTextColor(Color.parseColor(CommonUtils.getExpandedColorHex(clientInfo.textColor)))
    }

    private fun setActivityActive(isShown: Boolean) {
        isActivityVisible = isShown
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")
        logEvent(LogLevel.DEBUG, "$TAG:onActivityResult", null)
        verloopFragment.fileUploadResult(requestCode, resultCode, data)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
        } else {
            // Permission not granted. Notifications will be disabled.
        }
    }

    private fun logEvent(level: LogLevel, message: String, params: JSONObject?) {
        if (config?.logLevel?.ordinal!! >= level.ordinal) {
            viewModel?.logEvent(LogEvent(level.name, message, params))
        }
    }
}