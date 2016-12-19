package cameraHolder;

import observer.Observer;

import java.util.Map;

public abstract class CameraHolder implements Observer {
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

    public abstract void closeConnection();

    /* Getters and setters */
    public boolean isConnected() {
        return connected;
    }

    public int getHorizontalAngle() {
        return horizontalAngle;
    }

    public void setHorizontalAngle(int horizontalAngle) {
        if (horizontalAngle < horizontalAngleMinValue || horizontalAngle > horizontalAngleMaxValue) {
            System.err.println("Received angle's value is out of bound");
        } else {
            this.horizontalAngle = horizontalAngle;
        }
    }

    public int getVerticalAngle() {
        return verticalAngle;
    }

    public void setVerticalAngle(int verticalAngle) {

        if (verticalAngle < verticalAngleMinValue || verticalAngle > verticalAngleMaxValue) {
            System.err.println("Received angle's value is out of bound");
        } else {
            this.verticalAngle = verticalAngle;
        }
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
