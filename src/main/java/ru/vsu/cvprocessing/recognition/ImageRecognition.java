package ru.vsu.cvprocessing.recognition;

import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import ru.vsu.cvprocessing.event.SendingDataEvent;
import ru.vsu.cvprocessing.event.SendingDataPublisher;
import ru.vsu.cvprocessing.holder.Camera;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

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
    protected Color markerColor;

    protected int coordinateChangeCounter;
    protected int refreshCoordinateFrequency;

    public ImageRecognition() {
        videoCapture = new VideoCapture();

        coordinateChangeCounter = 0;
        refreshCoordinateFrequency = 1;
        xCoordinate = 0;
        yCoordinate = 0;
        markerColor = new Color(1, 0, 0, 1);
    }

    /* Open/Close video capture methods */
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

    protected void publishCoordinates() {
        SendingDataPublisher sendingDataPublisher = getInstance().getApplicationContext().getBean(SendingDataPublisher.class);

        int horizontalShift = (int) Math.round(
                (camera.getWidth() / 2 - xCoordinate) * camera.getHorizontalFieldOfView() / camera.getWidth()
        );
        int horizontalAngle = getInstance().getCameraHolder().getHorizontalAngle() + horizontalShift;
        sendingDataPublisher.publish(new SendingDataEvent(objectDetected, false, horizontalAngle));

        int verticalShift = (int) Math.round(
                (yCoordinate - camera.getHeight() / 2) * camera.getVerticalFieldOfView() / camera.getHeight()
        );
        int verticalAngle = getInstance().getCameraHolder().getVerticalAngle() + verticalShift;
        sendingDataPublisher.publish(new SendingDataEvent(objectDetected, true, verticalAngle));
    }

    /* Accessory method */
    abstract protected Image grabFrame(Map<String, Object> parameters);

    protected static Image matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();// create a temporary buffer
        Imgcodecs.imencode(".png", frame, buffer);// encode the frame in the buffer, according to the PNG format
        return new Image(new ByteArrayInputStream(buffer.toArray()));// build and return an Image
    }

    /* Getters and setters  */
    public ImageRecognitionMethod getImageRecognitionMethod() {
        return imageRecognitionMethod;
    }

    public Camera getCamera() {
        return camera;
    }

    public int getxCoordinate() {
        return xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }

    public boolean isObjectDetected() {
        return objectDetected;
    }

    public Color getMarkerColor() {
        return markerColor;
    }

    public int getCoordinateChangeCounter() {
        return coordinateChangeCounter;
    }

    public int getRefreshCoordinateFrequency() {
        return refreshCoordinateFrequency;
    }

    protected void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void setMarkerColor(Color markerColor) {
        this.markerColor = markerColor;
    }

    public void setCoordinateChangeCounter(int coordinateChangeCounter) {
        this.coordinateChangeCounter = coordinateChangeCounter;
    }

    public void setRefreshCoordinateFrequency(int refreshCoordinateFrequency) {
        if (refreshCoordinateFrequency <= 0) {
            log.error(String.format("Wrong refresh coordinate frequency value (%s)", refreshCoordinateFrequency));
        } else {
            this.refreshCoordinateFrequency = refreshCoordinateFrequency;
            log.info("Refresh coordinate frequency set to " + refreshCoordinateFrequency);
        }
    }
}
