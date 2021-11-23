package io.verloop.sdk.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.verloop.sdk.model.ClientInfo
import io.verloop.sdk.repository.VerloopRepository

class MainViewModel(var repository: VerloopRepository) : ViewModel() {
    var details: MutableLiveData<ClientInfo>? = null
    fun getClientInfo(): LiveData<ClientInfo>? {
        details = repository.getClientInfo()
        return details
    }
}