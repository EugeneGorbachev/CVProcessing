package ru.vsu.cvprocessing.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vsu.cvprocessing.holder.Camera;
import ru.vsu.cvprocessing.holder.CameraHolder;
import ru.vsu.cvprocessing.holder.ServoMotorControl;
import ru.vsu.cvprocessing.recognition.FakeImageRecognition;
import ru.vsu.cvprocessing.recognition.ImageRecognition;

public class SettingsHolder {
    public final static String FXML_FILE_PREF = "../../../../fxml/";
    public final static String CASCADE_FILE_PREF = "../../../../haarcascades/";

    /* Singleton */
    private static SettingsHolder instance = null;

    public static SettingsHolder getInstance() {
        if (instance == null) {
            Camera cam = new Camera(0, 40, 20, 340d, 495d);

            instance = new SettingsHolder(
                    cam,
                    new Color(1, 0, 0, 1),
                    new ServoMotorControl.ServoMotorControlBuilder()
                            .setHorizontalAngleMinValue(0)
                            .setHorizontalAngleMaxValue(180)
                            .setVerticalAngleMinValue(0)
                            .setVerticalAngleMaxValue(180)
                            .build(),
                    new FakeImageRecognition() {{
                        setCamera(cam);
                    }},
                    new Color(1, 0, 0, 1),
                    new Color(0, 0, 1, 1),
                    null
            );
        }
        return instance;
    }

    private ConfigurableApplicationContext applicationContext;

    /* Image preferences */
    private Camera camera;
    private ObjectProperty<Color> markerColorProperty;

    /* Abstract classes fields */
    private CameraHolder cameraHolder;
    private ImageRecognition imageRecognition;

    /* Recognize by color preferences */
    private ObjectProperty<Color> colorRangeStartProperty;
    private ObjectProperty<Color> colorRangeEndProperty;

    /* Recognize by cascade preferences */
    private String haarCascadeConfigFilename;

    /* Others */
    private BooleanProperty showSelectedPixelColor;
    private BooleanProperty sendDetectionData;

    private SettingsHolder(Camera camera, Color markerColor, CameraHolder cameraHolder, ImageRecognition imageRecognition,
                           Color colorRangeStart, Color colorRangeEnd, String haarCascadeConfigFilename) {
        this.cameraHolder = cameraHolder;
        this.imageRecognition = imageRecognition;
        this.camera = camera;
        this.markerColorProperty = new ColorProperty(markerColor, "Marker color");
        this.markerColorProperty.addListener(((observable, oldValue, newValue) -> imageRecognition.setMarkerColor(newValue)));
        this.colorRangeStartProperty = new ColorProperty(colorRangeStart, "Image recognition color range start");
        this.colorRangeEndProperty = new ColorProperty(colorRangeEnd, "Image recognition color range end");
        this.haarCascadeConfigFilename = haarCascadeConfigFilename;
        this.showSelectedPixelColor = new SimpleBooleanProperty(true);
        this.sendDetectionData = new SimpleBooleanProperty(false);
    }

    /* Property Getters */
    public Color getMarkerColorProperty() {
        return markerColorProperty.get();
    }

    public ObjectProperty<Color> markerColorPropertyProperty() {
        return markerColorProperty;
    }

    public Color getColorRangeStartProperty() {
        return colorRangeStartProperty.get();
    }

    public ObjectProperty<Color> colorRangeStartPropertyProperty() {
        return colorRangeStartProperty;
    }

    public Color getColorRangeEndProperty() {
        return colorRangeEndProperty.get();
    }

    public ObjectProperty<Color> colorRangeEndPropertyProperty() {
        return colorRangeEndProperty;
    }

    public boolean isShowSelectedPixelColor() {
        return showSelectedPixelColor.get();
    }

    public BooleanProperty showSelectedPixelColorProperty() {
        return showSelectedPixelColor;
    }

    public boolean getSendDetectionData() {
        return sendDetectionData.get();
    }

    public BooleanProperty sendDetectionDataProperty() {
        return sendDetectionData;
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

    public String getHaarCascadeConfigFilename() {
        return haarCascadeConfigFilename;
    }

    public void setHaarCascadeConfigFilename(String haarCascadeConfigFilename) {
        this.haarCascadeConfigFilename = haarCascadeConfigFilename;
    }
}