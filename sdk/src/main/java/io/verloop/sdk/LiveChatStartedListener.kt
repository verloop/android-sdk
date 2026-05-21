package io.verloop.sdk

fun interface LiveChatStartedListener {
    fun onChatStarted(roomId: String?)
}