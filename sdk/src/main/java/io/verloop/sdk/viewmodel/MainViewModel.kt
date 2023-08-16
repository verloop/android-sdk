package io.verloop.sdk.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.verloop.sdk.Verloop
import io.verloop.sdk.model.ClientInfo
import io.verloop.sdk.model.LogEvent
import io.verloop.sdk.repository.VerloopRepository

class MainViewModel(var configKey: String?, var repository: VerloopRepository) : ViewModel() {

    private var details: MutableLiveData<ClientInfo>? = null

    companion object {
        const val TAG = "MainViewModel"
    }

    fun getClientInfo(): LiveData<ClientInfo>? {
        details = repository.getClientInfo()
        return details
    }

    fun buttonClicked(json: String) {
        configKey.let {
            Verloop.eventListeners[configKey]?.onButtonClick(json)
        }
    }

    fun urlClicked(json: String) {
        configKey.let {
            Verloop.eventListeners[configKey]?.onURLClick(json)
        }
    }

    fun logEvent(event: LogEvent) {
        Log.d(TAG, event.toString())
        configKey.let {
            Verloop.eventListeners[configKey]?.onLogEvent(event)
        }
    }
}