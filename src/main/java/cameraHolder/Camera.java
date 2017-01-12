package cameraHolder;

public class Camera {
    private int webcamIndex;
    private int fieldOfView;
    private double height;
    private double width;

    public Camera(int webcamIndex, int fieldOfView, double height, double width) {
        this.webcamIndex = webcamIndex;
        this.fieldOfView = fieldOfView;
        this.height = height;
        this.width = width;
    }

    public String getWebcamName() {
        return "";//webcam.getName();
    }

    /* Getters and setters */
    public int getWebcamIndex() {
        return webcamIndex;
    }

    public void setWebcamIndex(int webcamIndex) {
        this.webcamIndex = webcamIndex;
    }

    public int getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(int fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }
    /* Getters and setters */
}
