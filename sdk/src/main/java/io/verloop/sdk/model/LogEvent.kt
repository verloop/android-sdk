package io.verloop.sdk.model

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
data class LogEvent(
    var type: String, var message: String, var params: JSONObject? = null
)