package io.verloop.sdk;

public class ClientInfoEvent {
    private String title;
    private String bgColor;
    private String textColor;

    public ClientInfoEvent(String title, String bgColor, String textColor) {
        this.title = title;
        this.bgColor = bgColor;
        this.textColor = textColor;
    }

    public String getTitle() {
        return title;
    }

    public String getBgColor() {
        return bgColor;
    }

    public String getTextColor() {
        return textColor;
    }
}
