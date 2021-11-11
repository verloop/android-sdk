package com.example.verloop_sdk.api

import io.verloop.sdk.model.UIModel
import retrofit2.Call
import retrofit2.http.GET

interface VerloopAPI {

    @GET("livechatInit")
    fun getUIDetails(): Call<UIModel>

}