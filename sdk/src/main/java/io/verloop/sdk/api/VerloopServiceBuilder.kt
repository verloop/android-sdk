package io.verloop.sdk.api

import android.content.Context
import io.verloop.sdk.utils.NetworkUtils
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object VerloopServiceBuilder {

    fun <T> buildService(context: Context, baseUrl: String, service: Class<T>): Retrofit {
        val cacheSize = (5 * 1024 * 1024).toLong()
        val myCache = Cache(context.cacheDir, cacheSize)
        val okHttpClient = OkHttpClient.Builder()
            .cache(myCache)
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (NetworkUtils.isNetworkAvailable(context))
                // Don't hit the API in case last call was less than 30 seconds ago
                    request.newBuilder().header("Cache-Control", "public, max-age=" + 30).build()
                else
                // Cache data can be used for 7 days in  case network is not available
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7
                    ).build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
}