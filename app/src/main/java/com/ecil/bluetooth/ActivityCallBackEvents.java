package com.ecil.bluetooth;

public class ActivityCallBackEvents {
    public static final int GENERATE_REPORT = 0;
    public static final int FINISH_PATIENTINFO_ACTIVITY = 0;
    int eventType;

    public ActivityCallBackEvents(int eventType) {
        this.eventType = eventType;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
