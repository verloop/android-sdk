package io.verloop.sdk

import io.verloop.sdk.model.LogEvent

interface LiveLogEventListener {
    fun logEvent(event: LogEvent)
}