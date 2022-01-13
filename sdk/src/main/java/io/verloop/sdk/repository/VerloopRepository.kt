package io.verloop.sdk.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.verloop.sdk.api.VerloopAPI
import io.verloop.sdk.model.ClientInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import android.content.Context.MODE_PRIVATE

import android.content.SharedPreferences
import com.google.gson.Gson

class VerloopRepository(val context: Context, private val retrofit: Retrofit) {

    var sharedPreferences: SharedPreferences = context.getSharedPreferences("MySharedPref", MODE_PRIVATE)

    fun getClientInfo(): MutableLiveData<ClientInfo> {
        val details = MutableLiveData<ClientInfo>()

        // If available return data stored in sharedPreferences first and then hit the API in background
        var clientInfoJson = sharedPreferences.getString("clientInfo", null)
        if(clientInfoJson != null) {
            val clientInfo = Gson().fromJson(clientInfoJson, ClientInfo::class.java)
            details.value = clientInfo
        }

        // API call to get new data if available
        val call = retrofit.create(VerloopAPI::class.java).getClientInfo()
        call.enqueue(object : Callback<ClientInfo> {
            override fun onResponse(call: Call<ClientInfo>, response: Response<ClientInfo>) {
                val data = response.body()
                if(data != null) {
                    val clientInfo = ClientInfo(data.title, data.textColor, data.bgColor);
                    details.value = clientInfo

                    val myEdit = sharedPreferences.edit()
                    myEdit.putString("clientInfo", Gson().toJson(clientInfo))
                    myEdit.commit()
                }
            }

            override fun onFailure(call: Call<ClientInfo>, t: Throwable) {
                Log.e("FAIL", t.message.toString())
            }
        })
        return details
    }
}