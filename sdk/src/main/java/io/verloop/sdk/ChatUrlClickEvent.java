package io.verloop.sdk;

public class ChatUrlClickEvent {
    private String url;

    ChatUrlClickEvent(String url) {
        this.url = url;
    }

    String getUrl() {
        return url;
    }
}
