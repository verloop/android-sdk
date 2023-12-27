package io.verloop.sdk.model

import androidx.annotation.Keep

@Keep
data class ClientInfo(
    var title: String?,
    var textColor: String?,
    var bgColor: String?,
    var livechatSettings: LivechatSettings?
)