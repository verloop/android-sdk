package io.verloop.sdk.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.verloop.sdk.api.VerloopServiceBuilder
import com.example.verloop_sdk.api.VerloopAPI
import io.verloop.sdk.model.UIModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object VerloopRepository {
    val details = MutableLiveData<UIModel>()
    fun getUIDetails(): MutableLiveData<UIModel> {
        val call = VerloopServiceBuilder.buildService(VerloopAPI::class.java).getUIDetails()
        call.enqueue(object : Callback<UIModel> {
            override fun onResponse(call: Call<UIModel>, response: Response<UIModel>) {
                val data = response.body()
                if(data != null) {
                    val title = data.title
                    val textColor = data.textColor
                    val bgColor = data.bgColor
                    details.value = UIModel(title, textColor, bgColor)
                }
            }

            override fun onFailure(call: Call<UIModel>, t: Throwable) {
                Log.e("FAIL", t.message.toString())
            }
        })
        return details
    }
}