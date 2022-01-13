package io.verloop.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.verloop.sdk.repository.VerloopRepository

class MainViewModelFactory(private val configKey: String?, private val repository: VerloopRepository) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)){
            return MainViewModel(configKey, repository) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}