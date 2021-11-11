package io.verloop.sdk.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object VerloopServiceBuilder {
    private const val BASE_URL = "https://hello.verloop.io/"

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }
}