package ru.vsu.cvprocessing.holder;

import org.apache.log4j.Logger;

import java.util.Map;

public abstract class CameraHolder {
    private static final Logger log = Logger.getLogger(CameraHolder.class);

    private final int horizontalAngleMinValue;
    private final int horizontalAngleMaxValue;
    private final int verticalAngleMinValue;
    private final int verticalAngleMaxValue;

    private int horizontalAngle;
    private int verticalAngle;
    boolean connected;

    CameraHolder(int horizontalAngleMinValue, int horizontalAngleMaxValue, int verticalAngleMinValue, int verticalAngleMaxValue) {
        this.horizontalAngleMinValue = horizontalAngleMinValue;
        this.horizontalAngleMaxValue = horizontalAngleMaxValue;
        this.verticalAngleMinValue = verticalAngleMinValue;
        this.verticalAngleMaxValue = verticalAngleMaxValue;
    }

    public abstract void setUpConnection(Map<String, Object> parameters) throws Exception;

    public abstract boolean closeConnection();

    /* Getters and setters */
    public boolean isConnected() {
        return connected;
    }

    public int getHorizontalAngle() {
        return horizontalAngle;
    }

    //TODO remove duplicate
    public void setHorizontalAngle(int horizontalAngle) throws Exception {
        if (horizontalAngle < horizontalAngleMinValue) {
            throw new IndexOutOfBoundsException(String.format("Received horizontal angle's value (%d) less than min", horizontalAngle));
        }
        if (horizontalAngle > horizontalAngleMaxValue) {
            throw new IndexOutOfBoundsException(String.format("Received horizontal angle's value (%d) greater than max", horizontalAngle));
        }
        this.horizontalAngle = horizontalAngle;
    }

    public int getVerticalAngle() {
        return verticalAngle;
    }

    public void setVerticalAngle(int verticalAngle) throws Exception {
        if (verticalAngle < verticalAngleMinValue) {
            throw new IndexOutOfBoundsException(String.format("Received vertical angle's value (%d) less than min", verticalAngle));
        }
        if (verticalAngle > verticalAngleMaxValue) {
            throw new IndexOutOfBoundsException(String.format("Received vertical angle's value (%d) less than min", verticalAngle));
        }
        this.verticalAngle = verticalAngle;
    }

    /* Final fields getters */
    public int getHorizontalAngleMinValue() {
        return horizontalAngleMinValue;
    }

    public int getHorizontalAngleMaxValue() {
        return horizontalAngleMaxValue;
    }

    public int getVerticalAngleMinValue() {
        return verticalAngleMinValue;
    }

    public int getVerticalAngleMaxValue() {
        return verticalAngleMaxValue;
    }

}
