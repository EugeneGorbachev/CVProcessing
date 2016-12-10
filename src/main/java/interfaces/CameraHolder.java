package interfaces;

import java.util.Map;

public abstract class CameraHolder {
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

    public abstract boolean setUpConnection(Map<String, Object> parameters) throws Exception;

    public abstract void closeConnection();

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
