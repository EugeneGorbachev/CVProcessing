package ru.vsu.cvprocessing.event;

public class SendingDataEvent {
    private byte value;

    public SendingDataEvent(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
