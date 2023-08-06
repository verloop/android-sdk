package io.verloop.sdk.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.verloop.sdk.Verloop
import io.verloop.sdk.model.ClientInfo
import io.verloop.sdk.model.LogEvent
import io.verloop.sdk.repository.VerloopRepository

class MainViewModel(var configKey: String?, var repository: VerloopRepository) : ViewModel() {
    var details: MutableLiveData<ClientInfo>? = null

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
        configKey.let {
            Verloop.eventListeners[configKey]?.onLogEvent(event)
        }
    }
}