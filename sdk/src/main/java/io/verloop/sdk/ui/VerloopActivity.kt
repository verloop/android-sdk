package io.verloop.sdk.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.ViewModelProvider
import io.verloop.sdk.HideEventListener
import io.verloop.sdk.R
import io.verloop.sdk.Verloop.Companion.eventListeners
import io.verloop.sdk.Verloop.Companion.hideEventListeners
import io.verloop.sdk.Verloop.Companion.isActivityVisible
import io.verloop.sdk.VerloopConfig
import io.verloop.sdk.VerloopNotification
import io.verloop.sdk.api.VerloopAPI
import io.verloop.sdk.api.VerloopServiceBuilder.buildService
import io.verloop.sdk.model.ClientInfo
import io.verloop.sdk.repository.VerloopRepository
import io.verloop.sdk.viewmodel.MainViewModel
import io.verloop.sdk.viewmodel.MainViewModelFactory

class VerloopActivity : AppCompatActivity() {

    private lateinit var verloopFragment: VerloopFragment
    private var toolbar: Toolbar? = null
    private var config: VerloopConfig? = null
    private var viewModel: MainViewModel? = null

    companion object {
        const val TAG = "VerloopActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        this.config = config

        if (config != null) {
            val baseUrl =
                if (config.isStaging) "https://${config.clientId}.stage.verloop.io" else "https://${config.clientId}.verloop.io"

            val retrofit =
                buildService(
                    applicationContext,
                    baseUrl,
                    VerloopAPI::class.java
                )
            val repository = VerloopRepository(applicationContext, retrofit)
            val viewModelFactory = MainViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
            viewModel?.getClientInfo()!!
                .observe(this, { clientInfo -> updateClientInfo(clientInfo) })
            hideEventListeners[config.clientId] = object : HideEventListener {
                override fun onHide() {
                    onBackPressed()
                }
            }
            addFragment()
        }
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
        super.onDestroy()
        eventListeners.remove(config?.clientId)
        hideEventListeners.remove(config?.clientId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//      switch deleted
        if (item.itemId == android.R.id.home) {
            // Respond to the action bar's Up/Home button
            finish()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addFragment() {
        Log.d(TAG, "Add Fragment from Activity")
        verloopFragment = VerloopFragment.newInstance(config)
        Log.d(TAG, "Frag: " + (verloopFragment != null))

        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.verloop_layout, verloopFragment, "VerloopActivity#Fragment").commit()

        // So that the keyboard doesn't cover the text input button.
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun updateClientInfo(clientInfo: ClientInfo) {
        toolbar?.title = clientInfo.title
        toolbar?.setBackgroundColor(Color.parseColor(clientInfo.bgColor))
        if (clientInfo.textColor?.length == 4) {
            val textColor = clientInfo.textColor?.replace(
                "#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])".toRegex(),
                "#$1$1$2$2$3$3"
            )
            clientInfo.textColor = textColor
        }
        toolbar?.setTitleTextColor(Color.parseColor(clientInfo.textColor))
    }

    private fun setActivityActive(isShown: Boolean) {
        isActivityVisible = isShown
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")
        verloopFragment?.fileUploadResult(requestCode, resultCode, data)
    }
}