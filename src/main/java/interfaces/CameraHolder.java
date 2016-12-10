package interfaces;

import java.util.Map;

public abstract class CameraHolder {
    int horizontalAngle;
    int verticalAngle;
    boolean connected;

    public abstract boolean setUpConnection(Map<String, Object> parameters) throws Exception;

    public abstract void closeConnection() throws Exception;

    public boolean isConnected() {
        return connected;
    }

    public int getHorizontalAngle() {
        return horizontalAngle;
    }

    public void setHorizontalAngle(int horizontalAngle) {
        this.horizontalAngle = horizontalAngle;
    }

    public int getVerticalAngle() {
        return verticalAngle;
    }

    public void setVerticalAngle(int verticalAngle) {
        this.verticalAngle = verticalAngle;
    }
}
