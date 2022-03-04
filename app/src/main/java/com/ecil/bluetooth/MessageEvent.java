package com.ecil.bluetooth;

public class MessageEvent {
    public static final int TOAST_MESSAGE = 0;
    public static final int PROGRESS_DIALOG_START = 1;
    public static final int PROGRESS_DIALOG_STOP = 2;
    public static final int ALERT = 3;
    public static final int SHOW_HOME_SCREEN = 4;
    public static final int BLUETOOTH_DISCONNECTED = 5;


    String msg;
    String title;
    int eventType;



    public MessageEvent(int eventType) {
        this.eventType = eventType;
    }
    public MessageEvent(String data, int eventType) {
        this.msg = data;
        this.eventType = eventType;
    }

    public MessageEvent(String title, String msg, int eventType) {
        this.title = title;
        this.msg = msg;
        this.eventType = eventType;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String data) {
        this.msg = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
