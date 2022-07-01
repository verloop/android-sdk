package io.verloop.sdk.service

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.JsonObject
import io.verloop.sdk.api.VerloopAPI
import io.verloop.sdk.api.VerloopServiceBuilder
import io.verloop.sdk.model.LogoutRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogoutWorker(appContext: Context, params: WorkerParameters) :
    Worker(appContext, params) {
    private val TAG: String = "LogoutWorker"

    override fun doWork(): Result {
        val clientId = inputData.getString(LogoutRequestBody.CLIENT_ID)
        val userId = inputData.getString(LogoutRequestBody.USER_ID)
        val fcmToken = inputData.getString(LogoutRequestBody.FCM_TOKEN)
        val isStaging = inputData.getBoolean(LogoutRequestBody.IS_STAGING, false)

        if (clientId != null) {
            val baseUrl =
                if (isStaging) "https://${clientId}.stage.verloop.io" else "https://${clientId}.verloop.io"

            val retrofit =
                VerloopServiceBuilder.buildService(
                    applicationContext,
                    baseUrl,
                    VerloopAPI::class.java
                )
            val body = LogoutRequestBody(
                userId,
                "android",
                fcmToken
            )
            val call = retrofit.create(VerloopAPI::class.java).logout(body, clientId)
            call.enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.d(TAG, response.body().toString())
                    Toast.makeText(applicationContext, "Logout Successful", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.e(TAG, t.message.toString())
                    Toast.makeText(applicationContext, "Logout Failed", Toast.LENGTH_SHORT).show()
                }

            })
        }
        return Result.success()
    }
}