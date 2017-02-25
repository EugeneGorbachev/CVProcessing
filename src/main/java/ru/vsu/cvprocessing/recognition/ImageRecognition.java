package ru.vsu.cvprocessing.recognition;

import org.apache.log4j.Logger;
import ru.vsu.cvprocessing.holder.Camera;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.opencv.videoio.VideoCapture;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ImageRecognition {
    private static final Logger log = Logger.getLogger(ImageRecognition.class);

    protected ImageRecognitionMethod imageRecognitionMethod;
    protected final VideoCapture videoCapture;
    protected ScheduledExecutorService timer;
    protected Camera camera;

    //  Tracked object's center coordinate
    protected int xCoordinate;
    protected int yCoordinate;
    protected boolean objectDetected;
    protected int prevXCoordinate;
    protected int prevYCoordinate;
    protected Color markerColor;

    private int coordinateChangeCounter;
    private int refreshPrevCoordinateFrequency;

    public ImageRecognition() {
        videoCapture = new VideoCapture();

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
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try {
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.error(e);
            }
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
            log.error(String.format("Wrong refresh previous coordinate frequency value (%s)", refreshPrevCoordinateFrequency));
        } else {
            this.refreshPrevCoordinateFrequency = refreshPrevCoordinateFrequency;
        }
    }

    /* Getters */
    public ImageRecognitionMethod getImageRecognitionMethod() {
        return imageRecognitionMethod;
    }
    /* Getters */

    /* Getters and setters */

    public Camera getCamera() {
        return camera;
    }

    protected void setCamera(Camera camera) {
        this.camera = camera;
    }

    public boolean isObjectDetected() {
        return objectDetected;
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
