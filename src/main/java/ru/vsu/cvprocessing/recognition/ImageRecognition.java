package ru.vsu.cvprocessing.recognition;

import ru.vsu.cvprocessing.holder.Camera;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import ru.vsu.cvprocessing.observer.Observable;
import org.opencv.videoio.VideoCapture;

import java.util.Map;

public abstract class ImageRecognition implements Observable {
    ImageRecognitionMethod imageRecognitionMethod;
    VideoCapture videoCapture;
    Camera camera;

    //  Tracked object's center coordinate
    int xCoordinate;
    int yCoordinate;
    boolean objectDetected;
    int prevXCoordinate;
    int prevYCoordinate;
    Color markerColor;

    private int coordinateChangeCounter;
    private int refreshPrevCoordinateFrequency;

    public ImageRecognition(Camera camera) {
        videoCapture = new VideoCapture();
        this.camera = camera;

        coordinateChangeCounter = 0;
        refreshPrevCoordinateFrequency = 5;
        prevXCoordinate = xCoordinate = 0;
        prevYCoordinate = yCoordinate = 0;
        markerColor = new Color(1,0,0,1);
    }

    public abstract void openVideoCapture(Map<String, Object> parameters) throws Exception;

    public boolean closeVideoCapture() {
        if (videoCapture.isOpened()) {
            videoCapture.release();
        }
        return videoCapture.isOpened();
    }

    abstract Image grabFrame(Map<String, Object> parameters);

    boolean savePrevCoordinate() {
        if (++coordinateChangeCounter >= getRefreshPrevCoordinateFrequency()) {
            prevXCoordinate = xCoordinate;
            prevYCoordinate = yCoordinate;
            coordinateChangeCounter %= getRefreshPrevCoordinateFrequency();
            return true;
        }
        return false;
    }

    public int getRefreshPrevCoordinateFrequency() {
        return refreshPrevCoordinateFrequency;
    }

    public void setRefreshPrevCoordinateFrequency(int refreshPrevCoordinateFrequency) {
        if (refreshPrevCoordinateFrequency <= 0) {
            System.err.println("Wrong refreshPrevCoordinateFrequency value");
        } else {
            this.refreshPrevCoordinateFrequency = refreshPrevCoordinateFrequency;
        }
    }

    /* Getters and setters */
    public ImageRecognitionMethod getImageRecognitionMethod() {
        return imageRecognitionMethod;
    }

    public void setImageRecognitionMethod(ImageRecognitionMethod imageRecognitionMethod) {
        this.imageRecognitionMethod = imageRecognitionMethod;
    }

    public boolean isObjectDetected() {
        return objectDetected;
    }

    public void setObjectDetected(boolean objectDetected) {
        this.objectDetected = objectDetected;
    }

    /* Getters for tracked object's center coordinate */
    public int getxCoordinate() {
        return xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }

    public Color getMarkerColor() {
        return markerColor;
    }

    public void setMarkerColor(Color markerColor) {
        this.markerColor = markerColor;
    }
}
