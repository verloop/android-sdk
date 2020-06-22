package io.verloop.sdk;

public class ChatButtonClickEvent {
    private String type;
    private String title;
    private String payload;

    public ChatButtonClickEvent(String type, String title, String payload) {
        this.type = type;
        this.title = title;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getPayload() {
        return payload;
    }
}
