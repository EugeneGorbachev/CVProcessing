package ru.vsu.cvprocessing.settings;

import ru.vsu.cvprocessing.holder.Camera;
import ru.vsu.cvprocessing.holder.CameraHolder;
import ru.vsu.cvprocessing.holder.ServoMotorControl;
import ru.vsu.cvprocessing.recognition.FakeImageRecognition;
import ru.vsu.cvprocessing.recognition.ImageRecognition;
import javafx.scene.paint.Color;

public class SettingsHolder {
    /* Singleton */
    private static SettingsHolder instance = null;

    public static SettingsHolder getInstance() {
        if (instance == null) {
            Camera camera = new Camera(0, 70, 200d, 200d);

            instance = new SettingsHolder(
                    camera,
                    new Color(1, 0, 0, 1),
                    new ServoMotorControl(camera),
                    new FakeImageRecognition(camera),
                    new Color(1, 0, 0,1),
                    new Color(0, 0, 1, 1),
                    null
            );
        }
        return instance;
    }
    /* Singleton */

    /* Image preferences */
    private Camera camera;
    private Color markerColor;
    /* Image preferences */

    private CameraHolder cameraHolder;
    private ImageRecognition imageRecognition;

    /* Recognize by color preferences */
    private Color colorRangeStart;
    private Color colorRangeEnd;
    /* Recognize by color preferences */

    /* Recognize by cascade preferences */
    private String haarcascade;
    /* Recognize by cascade preferences */

    private SettingsHolder(Camera camera, Color markerColor, CameraHolder cameraHolder, ImageRecognition imageRecognition,
                           Color colorRangeStart, Color colorRangeEnd, String haarcascade) {
        this.cameraHolder = cameraHolder;
        this.imageRecognition = imageRecognition;
        this.camera = camera;
        this.markerColor = markerColor;
        this.colorRangeStart = colorRangeStart;
        this.colorRangeEnd = colorRangeEnd;
        this.haarcascade = haarcascade;
    }

    /* Getters and setters */
    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Color getMarkerColor() {
        return markerColor;
    }

    public void setMarkerColor(Color markerColor) {
        this.markerColor = markerColor;
    }

    public CameraHolder getCameraHolder() {
        return cameraHolder;
    }

    public void setCameraHolder(CameraHolder cameraHolder) {
        this.cameraHolder = cameraHolder;
    }

    public ImageRecognition getImageRecognition() {
        return imageRecognition;
    }

    public void setImageRecognition(ImageRecognition imageRecognition) {
        this.imageRecognition = imageRecognition;
    }

    public Color getColorRangeStart() {
        return colorRangeStart;
    }

    public void setColorRangeStart(Color colorRangeStart) {
        this.colorRangeStart = colorRangeStart;
    }

    public Color getColorRangeEnd() {
        return colorRangeEnd;
    }

    public void setColorRangeEnd(Color colorRangeEnd) {
        this.colorRangeEnd = colorRangeEnd;
    }

    public String getHaarcascade() {
        return haarcascade;
    }

    public void setHaarcascade(String haarcascade) {
        this.haarcascade = haarcascade;
    }
    /* Getters and setters */
}