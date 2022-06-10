package io.verloop.sdk

interface LiveChatButtonClickListener {
    fun buttonClicked(title: String?, type: String?, payload: String?)
}