package ru.vsu.cvprocessing.settings;

import org.springframework.context.ConfigurableApplicationContext;
import ru.vsu.cvprocessing.holder.Camera;
import ru.vsu.cvprocessing.holder.CameraHolder;
import ru.vsu.cvprocessing.holder.ServoMotorControl;
import ru.vsu.cvprocessing.recognition.FakeImageRecognition;
import ru.vsu.cvprocessing.recognition.ImageRecognition;
import javafx.scene.paint.Color;

public class SettingsHolder {
    public final static String FXML_FILE_PREF = "../../../../fxml/";

    /* Singleton */
    private static SettingsHolder instance = null;

    public static SettingsHolder getInstance() {
        if (instance == null) {
            Camera camera = new Camera(0, 70, 340d, 495d);

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

    private ConfigurableApplicationContext applicationContext;

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
    private String haarCascadeConfigFilename;
    /* Recognize by cascade preferences */

    private SettingsHolder(Camera camera, Color markerColor, CameraHolder cameraHolder, ImageRecognition imageRecognition,
                           Color colorRangeStart, Color colorRangeEnd, String haarCascadeConfigFilename) {
        this.cameraHolder = cameraHolder;
        this.imageRecognition = imageRecognition;
        this.camera = camera;
        this.markerColor = markerColor;
        this.colorRangeStart = colorRangeStart;
        this.colorRangeEnd = colorRangeEnd;
        this.haarCascadeConfigFilename = haarCascadeConfigFilename;
    }

    /* Getters and setters */

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

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

    public String getHaarCascadeConfigFilename() {
        return haarCascadeConfigFilename;
    }

    public void setHaarCascadeConfigFilename(String haarCascadeConfigFilename) {
        this.haarCascadeConfigFilename = haarCascadeConfigFilename;
    }
    /* Getters and setters */
}