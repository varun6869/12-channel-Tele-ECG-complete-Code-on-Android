package com.ecil.bluetooth;

public class CallBackEvents {
    public static final int GENERATE_REPORT = 0;
    public static final int FINISH_PATIENTINFO_ACTIVITY = 0;
    int eventType;

    public CallBackEvents(int eventType) {
        this.eventType = eventType;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
