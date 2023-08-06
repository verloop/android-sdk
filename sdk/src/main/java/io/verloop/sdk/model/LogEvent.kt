package io.verloop.sdk.model

import androidx.annotation.Keep

@Keep
data class LogEvent(
    var type: String,
    var message: String
)
