package io.verloop.sdk.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.verloop.sdk.model.UIModel
import io.verloop.sdk.repository.VerloopRepository

class MainViewModel : ViewModel() {
    var details: MutableLiveData<UIModel>? = null
    fun getUIDetails(): LiveData<UIModel>? {
        details = VerloopRepository.getUIDetails()
        return details
    }
}