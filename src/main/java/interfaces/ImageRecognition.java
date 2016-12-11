package interfaces;

import javafx.scene.image.Image;
import org.opencv.videoio.VideoCapture;

import java.util.Map;

public abstract class ImageRecognition {
    VideoCapture videoCapture;// todo may be private?
    int webCameraIndex;

    //  Tracked object's center coordinate
    int xCoordinate;
    int yCoordinate;

    public ImageRecognition() {
        videoCapture = new VideoCapture();
        webCameraIndex = 0;
        xCoordinate = 0;
        yCoordinate = 0;
    }

    public abstract void openVideoCapture(Map<String, Object> parameters) throws Exception;

    public void closeVideoCapture() {
        if (videoCapture.isOpened()) {
            videoCapture.release();
        }
    }

    abstract Image grabFrame(Map<String, Object> parameters);

    /* Getters and setters */
    public int getWebCameraIndex() {
        return webCameraIndex;
    }

    public void setWebCameraIndex(int webCameraIndex) {
        this.webCameraIndex = webCameraIndex;
    }

    /* Getters for tracked object's center coordinate */
    public int getxCoordinate() {
        return xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }
}
