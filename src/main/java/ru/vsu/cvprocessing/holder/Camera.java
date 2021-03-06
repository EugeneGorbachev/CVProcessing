package ru.vsu.cvprocessing.holder;

public class Camera {
    private int webCamIndex;
    private int horizontalFieldOfView;
    private int verticalFieldOfView;
    private double height;
    private double width;

    public Camera(int webCamIndex, int horizontalFieldOfView, int verticalFieldOfView, double height, double width) {
        this.webCamIndex = webCamIndex;
        this.horizontalFieldOfView = horizontalFieldOfView;
        this.verticalFieldOfView = verticalFieldOfView;
        this.height = height;
        this.width = width;
    }

    /* Getters and setters */
    public int getWebCamIndex() {
        return webCamIndex;
    }

    public void setWebCamIndex(int webCamIndex) {
        this.webCamIndex = webCamIndex;
    }

    public int getHorizontalFieldOfView() {
        return horizontalFieldOfView;
    }

    public void setHorizontalFieldOfView(int horizontalFieldOfView) {
        this.horizontalFieldOfView = horizontalFieldOfView;
    }

    public int getVerticalFieldOfView() {
        return verticalFieldOfView;
    }

    public void setVerticalFieldOfView(int verticalFieldOfView) {
        this.verticalFieldOfView = verticalFieldOfView;
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
