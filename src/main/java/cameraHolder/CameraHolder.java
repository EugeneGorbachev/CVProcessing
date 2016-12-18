package cameraHolder;

import observer.Observer;

import java.util.Map;

public abstract class CameraHolder implements Observer {
    final int horizontalAngleMinValue;
    final int horizontalAngleMaxValue;
    final int verticalAngleMinValue;
    final int verticalAngleMaxValue;

    int horizontalAngle;
    int verticalAngle;
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
        try {
            if (horizontalAngle < horizontalAngleMinValue || horizontalAngle > horizontalAngleMaxValue) {
                throw new Exception("Received angle's value is out of bound");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.horizontalAngle = horizontalAngle;
    }

    public int getVerticalAngle() {
        return verticalAngle;
    }

    public void setVerticalAngle(int verticalAngle) {
        try {
            if (verticalAngle < verticalAngleMinValue || verticalAngle > verticalAngleMaxValue) {
                throw new Exception("Received angle's value is out of bound");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
