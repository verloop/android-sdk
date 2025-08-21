package io.verloop.sdk.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import io.verloop.sdk.R
import io.verloop.sdk.Verloop.Companion.eventListeners
import io.verloop.sdk.Verloop.Companion.isActivityVisible
import io.verloop.sdk.VerloopConfig
import io.verloop.sdk.VerloopNotification
import io.verloop.sdk.api.VerloopAPI
import io.verloop.sdk.api.VerloopServiceBuilder.buildService
import io.verloop.sdk.enum.Position
import io.verloop.sdk.model.ClientInfo
import io.verloop.sdk.model.HeaderConfig
import io.verloop.sdk.model.LogEvent
import io.verloop.sdk.model.LogLevel
import io.verloop.sdk.repository.VerloopRepository
import io.verloop.sdk.utils.CommonUtils
import io.verloop.sdk.viewmodel.MainViewModel
import io.verloop.sdk.viewmodel.MainViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL


object Constants {
    const val ACTION_CLOSE_VERLOOP_WIDGET = "io.verloop.CLOSE_VERLOOP_WIDGET"
    const val ACTION_VERLOOP_WIDGET_TO_BACKGROUND = "io.verloop.PUSH_VERLOOP_WIDGET_TO_BACKGROUND"
}

class VerloopActivity : AppCompatActivity() {

    private lateinit var verloopFragment: VerloopFragment
    private lateinit var toolbar: Toolbar
    private var config: VerloopConfig? = null
    private var viewModel: MainViewModel? = null
    private var configKey: String? = null

    private var brandLogo: ImageView? = null
    private var tvTitle: TextView? = null
    private var tvSubTitle: TextView? = null

    private val closeActivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Constants.ACTION_CLOSE_VERLOOP_WIDGET) {
                logEvent(LogLevel.DEBUG, "$TAG:onClosingEvent", null)
                finish()
            }
        }
    }
    private val putActivityInBackgroundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Constants.ACTION_CLOSE_VERLOOP_WIDGET) {
                logEvent(LogLevel.DEBUG, "$TAG:onPushingToBackground", null)
                moveTaskToBack(true)
            }
        }
    }

    companion object {
        const val TAG = "VerloopActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verloop)

        toolbar = findViewById(R.id.verloop_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // Apply insets to toolbar so it doesn’t overlap status bar (android >=15)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,   // push toolbar content below status bar
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        brandLogo = toolbar.findViewById(R.id.verloop_brand_logo)
        tvTitle = toolbar.findViewById(R.id.toolbar_title)
        tvSubTitle = toolbar.findViewById(R.id.toolbar_subtitle)

        this.config = getConfig()

        configKey = intent.getStringExtra("configKey")

        if (config != null) {
            logEvent(LogLevel.DEBUG, "$TAG:onCreate", null)

            val baseUrl = getBaseUrl()
            val retrofit = buildService(applicationContext, baseUrl, VerloopAPI::class.java)
            val repository = VerloopRepository(applicationContext, retrofit)
            val viewModelFactory = MainViewModelFactory(configKey, repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

            if (this.config!!.overrideHeaderLayout) {
                useCustomHeaderLayout()
            } else {
                useDefaultHeader()
            }
            addFragment()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) requestPermissionLauncher.launch(
            Manifest.permission.POST_NOTIFICATIONS
        )
        // Register broadcast receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Context.RECEIVER_EXPORTED
            } else {
                Context.RECEIVER_NOT_EXPORTED
            }
            // For Android 13 (API 33) and above
            registerReceiver(
                closeActivityReceiver,
                IntentFilter(Constants.ACTION_CLOSE_VERLOOP_WIDGET),
                flags
            )
            registerReceiver(
                putActivityInBackgroundReceiver,
                IntentFilter(Constants.ACTION_VERLOOP_WIDGET_TO_BACKGROUND),
                flags
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 (API 33)
            registerReceiver(
                closeActivityReceiver,
                IntentFilter(Constants.ACTION_CLOSE_VERLOOP_WIDGET),
                Context.RECEIVER_EXPORTED
            )
        }
        else {
            // For older Android versions
            registerReceiver(
                closeActivityReceiver,
                IntentFilter(Constants.ACTION_CLOSE_VERLOOP_WIDGET)
            )
            registerReceiver(
                putActivityInBackgroundReceiver,
                IntentFilter(Constants.ACTION_VERLOOP_WIDGET_TO_BACKGROUND)
            )
        }

    }

    private fun getBaseUrl(): String {
        return if (this.config!!.isStaging) "https://${this.config!!.clientId}.stage.verloop.io"
        else "https://${this.config!!.clientId}.verloop.io"
    }

    private fun getConfig(): VerloopConfig? {
        return IntentCompat.getParcelableExtra(intent, "config", VerloopConfig::class.java)
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
        unregisterReceiver(closeActivityReceiver)
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
        Log.d(TAG, "updateClientInfo:  + ${clientInfo?.toString()}")
        logEvent(LogLevel.DEBUG, "$TAG:updateClientInfo", null)
        if (clientInfo?.livechatSettings?.Header != null) {
            val settings = clientInfo.livechatSettings
            val headerConfig: HeaderConfig = HeaderConfig.Builder()
                .brandLogo(settings?.Header?.BrandLogo?.URL.toString())
                .title(settings?.Header?.Title?.Heading.toString())
                .titleColor(CommonUtils.getExpandedColorHex(clientInfo.textColor))
                .titlePosition(Position.valueOf(settings?.Header?.Title?.Position.toString()))
                .titleFontSize(18.0f)
                .subtitle(settings?.Header?.Subtitle?.Heading.toString())
                .subtitlePosition(Position.valueOf(settings?.Header?.Subtitle?.Position.toString()))
                .subtitleColor(CommonUtils.getExpandedColorHex(clientInfo.textColor))
                .subtitleFontSize(12.0f)
                .backgroundColor(settings?.Theme?.ColorPalette?.Primary.toString())
                .build()

            if (config?.headerConfig != null) {
                config?.headerConfig?.overrideConfig(headerConfig)
                useCustomHeaderConfig(config?.headerConfig)
            } else {
                useCustomHeaderConfig(headerConfig)
            }
        } else if (config?.headerConfig != null) {
            useCustomHeaderConfig(config?.headerConfig)
        } else {
            toolbar?.setBackgroundColor(Color.parseColor(clientInfo.bgColor ?: "#FFFFFF"))
            toolbar?.title = clientInfo.title
            tvTitle?.text = clientInfo.title
            tvTitle?.setTextColor(Color.parseColor(CommonUtils.getExpandedColorHex(clientInfo.textColor)))
        }
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

    private fun useDefaultHeader() {
        // Use default config for header
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.navigationIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                ContextCompat.getColor(applicationContext, R.color.white), BlendModeCompat.SRC_ATOP
            )

        viewModel?.getClientInfo()
            ?.observe(this) { clientInfo ->
                if (clientInfo != null) {
                    updateClientInfo(clientInfo)
                }
            }
    }

    private fun useCustomHeaderLayout() {
        // Use custom xml header layout from main app
        findViewById<ImageView>(R.id.verloop_back_icon)?.setOnClickListener {
            finish()
        }
    }

    private fun useCustomHeaderConfig(headerConfig: HeaderConfig?) {
        if (headerConfig == null) return
        // Use custom header configuration from main app
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.navigationIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                ContextCompat.getColor(applicationContext, R.color.white), BlendModeCompat.SRC_ATOP
            )

        if (headerConfig != null) {
            headerConfig.brandLogo?.let {
                updateBrandLogo(it)
            }
            headerConfig.backgroundColor?.let {
                toolbar.setBackgroundColor(Color.parseColor(it))

                val color =
                    Color.parseColor(if (headerConfig.titleColor != null) headerConfig.titleColor else "#FFFFFF")
                val drawable = toolbar.navigationIcon
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    drawable?.setColorFilter(
                        BlendModeColorFilter(
                            color,
                            BlendMode.SRC_ATOP
                        )
                    )
                } else {
                    drawable?.setColorFilter(
                        color,
                        PorterDuff.Mode.SRC_ATOP
                    );
                }
            }

            headerConfig.title?.let { ito ->
                tvTitle?.text = ito
                headerConfig.titleColor?.let { tvTitle?.setTextColor(Color.parseColor(it)) }
                headerConfig.titleFontSize?.let { tvTitle?.textSize = it }
            }

            headerConfig.subtitle?.let { ito ->
                if (ito.isNotEmpty()) {
                    tvSubTitle?.visibility = View.VISIBLE
                    tvSubTitle?.text = ito
                    headerConfig.subtitleColor?.let { tvSubTitle?.setTextColor(Color.parseColor(it)) }
                    headerConfig.subtitleFontSize?.let { tvSubTitle?.textSize = it }
                }
            }


            headerConfig.titlePosition?.let {
                tvTitle?.gravity = getGravity(it)
                tvTitle?.layoutParams =
                    getLayoutParamsForCenterAlignment(headerConfig.brandLogo, it)
            }
            headerConfig.subtitlePosition?.let {
                tvSubTitle?.gravity = getGravity(it)
                tvSubTitle?.layoutParams =
                    getLayoutParamsForCenterAlignment(headerConfig.brandLogo, it)
            }
        }
    }

    private fun getLayoutParamsForCenterAlignment(
        brandLogo: String?,
        position: Position
    ): LinearLayout.LayoutParams {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(
            0,
            0,
            if (position == Position.CENTER) {
                CommonUtils.pxFromDp(
                    applicationContext,
                    if (!brandLogo.isNullOrEmpty()) 72 else 36
                ).toInt()
            } else {
                0
            },
            0
        )
        return layoutParams
    }

    private fun updateBrandLogo(url: String) {
        GlobalScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                val bmp = BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
                GlobalScope.launch(Dispatchers.Main + coroutineExceptionHandler) {
                    brandLogo?.setImageBitmap(bmp)
                    brandLogo?.setBackgroundResource(R.drawable.circle)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        brandLogo?.outlineProvider = ViewOutlineProvider.BOUNDS
                    }
                    brandLogo?.visibility = View.VISIBLE
                }
            } catch (e: MalformedURLException) {
                Log.w(TAG, "Failed to load brand logo. " + e.message);
            }
        }
    }

    private fun getGravity(position: Position?): Int {
        return when (position?.ordinal) {
            1 -> Gravity.CENTER
            2 -> Gravity.RIGHT
            else -> {
                Gravity.LEFT.or(Gravity.CENTER_VERTICAL)
            }
        }
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.w(TAG, throwable.message.toString())
    }
}