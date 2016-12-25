package cameraHolder;

import com.github.sarxos.webcam.Webcam;

public class Camera {
    private int webcamIndex;
    private Webcam webcam;

    private int fieldOfView;

    public Camera(int webcamIndex, Webcam webcam) {
        this.webcamIndex = webcamIndex;
        this.webcam = webcam;
        fieldOfView = 70;// default
    }

    public String getWebcamName() {
        return webcam.getName();
    }

    /* Getters and setters */
    public int getWebcamIndex() {
        return webcamIndex;
    }

    public void setWebcamIndex(int webcamIndex) {
        this.webcamIndex = webcamIndex;
    }

    public Webcam getWebcam() {
        return webcam;
    }

    public void setWebcam(Webcam webcam) {
        this.webcam = webcam;
    }

    public double getHeight() {
        return webcam.getViewSize().getHeight();
    }

    public void setHeight(double height) {
        webcam.getViewSize().setSize(webcam.getViewSize().getWidth(), height);
    }

    public double getWidth() {
        return webcam.getViewSize().getWidth();
    }

    public void setWidth(double width) {
        webcam.getViewSize().setSize(width, webcam.getViewSize().height);
    }

    public int getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(int fieldOfView) {
        this.fieldOfView = fieldOfView;
    }
    /* Getters and setters */
}
