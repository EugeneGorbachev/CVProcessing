package ru.vsu.cvprocessing.event;

public class SendingDataEvent {
    private boolean detected;
    private boolean vertical;
    private boolean joystickControl;
    private int value;

    public SendingDataEvent(boolean detected, boolean vertical, boolean joystickControl, int value) {
        this.detected = detected;
        this.vertical = vertical;
        this.joystickControl = joystickControl;
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
        if (joystickControl) {
            preferences = preferences | (1 << 2);
        }
        return preferences;
    }
    public boolean isDetected() {
        return detected;
    }

    public boolean isVertical() {
        return vertical;
    }

    public boolean isJoystickControl() {
        return joystickControl;
    }

    public int getValue() {
        return value;
    }
}
