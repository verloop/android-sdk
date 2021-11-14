package io.verloop.sdk.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.verloop.sdk.model.ClientInfo
import io.verloop.sdk.repository.VerloopRepository

class MainViewModel : ViewModel() {
    var details: MutableLiveData<ClientInfo>? = null
    fun getUIDetails(): LiveData<ClientInfo>? {
        details = VerloopRepository.getUIDetails()
        return details
    }
}