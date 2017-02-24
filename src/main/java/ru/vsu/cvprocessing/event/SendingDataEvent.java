package ru.vsu.cvprocessing.event;

public class SendingDataEvent {
    private boolean detected;
    private boolean vertical;
    private int value;

    public SendingDataEvent(boolean detected, boolean vertical, int value) {
        this.detected = detected;
        this.vertical = vertical;
        this.value = value;
    }

    public int getPreferences() {
        int preferences = 0;
        if (detected) {
            preferences = preferences | (1 << 0);
        }
        if (vertical) {
            preferences = preferences | (1 << 1);
        }
        preferences = preferences | (1 << 2);
        return preferences;
    }
    public boolean isDetected() {
        return detected;
    }

    public boolean isVertical() {
        return vertical;
    }

    public int getValue() {
        return value;
    }
}
