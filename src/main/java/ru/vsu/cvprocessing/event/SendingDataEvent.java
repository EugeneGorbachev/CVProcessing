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
