package io.verloop.sdk.api

import com.google.gson.JsonObject
import io.verloop.sdk.model.ClientInfo
import io.verloop.sdk.model.LogoutRequestBody
import retrofit2.Call
import retrofit2.http.*

interface VerloopAPI {

    @GET("livechatInit")
    fun getClientInfo(): Call<ClientInfo>

    @Headers("Content-Type: application/json")
    @POST("api/public/sdk/unregisterDevice")
    fun logout(
        @Body body: LogoutRequestBody,
        @Header("x-verloop-client-id") clientId: String
    ): Call<JsonObject>

}