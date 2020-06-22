package io.verloop.sdk;

public class ClientInfoEvent {
    private String title;
    private String bgColor;
    private String textColor;

    ClientInfoEvent(String title, String bgColor, String textColor) {
        this.title = title;
        this.bgColor = bgColor;
        this.textColor = textColor;
    }

    String getTitle() {
        return title;
    }

    String getBgColor() {
        return bgColor;
    }

    String getTextColor() {
        return textColor;
    }
}
