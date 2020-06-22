package io.verloop.sdk;

public class ChatButtonClickEvent {
    private String type;
    private String title;
    private String payload;

    ChatButtonClickEvent(String type, String title, String payload) {
        this.type = type;
        this.title = title;
        this.payload = payload;
    }

    String getType() {
        return type;
    }

    String getTitle() {
        return title;
    }

    String getPayload() {
        return payload;
    }
}
