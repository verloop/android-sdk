package io.verloop.sdk.model

import androidx.annotation.Keep

@Keep
data class LogoutRequestBody(var userId: String?, var deviceType: String?, var deviceToken: String?) {

    companion object {
        const val CLIENT_ID = "CLIENT_ID"
        const val USER_ID = "user_id"
        const val FCM_TOKEN = "fcm_token"
    }
}