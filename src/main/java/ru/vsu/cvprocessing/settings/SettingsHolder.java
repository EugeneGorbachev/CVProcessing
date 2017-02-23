package ru.vsu.cvprocessing.settings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vsu.cvprocessing.holder.Camera;
import ru.vsu.cvprocessing.holder.CameraHolder;
import ru.vsu.cvprocessing.holder.ServoMotorControl;
import ru.vsu.cvprocessing.recognition.FakeImageRecognition;
import ru.vsu.cvprocessing.recognition.ImageRecognition;
import javafx.scene.paint.Color;

public class SettingsHolder {
    private static final Logger log = Logger.getLogger(SettingsHolder.class);

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
    private ObjectProperty<Color> markerColorProperty;
    /* Image preferences */

    private CameraHolder cameraHolder;
    private ImageRecognition imageRecognition;

    /* Recognize by color preferences */
    private ObjectProperty<Color> colorRangeStartProperty;
    private ObjectProperty<Color> colorRangeEndProperty;
    /* Recognize by color preferences */

    /* Recognize by cascade preferences */
    private String haarCascadeConfigFilename;
    /* Recognize by cascade preferences */

    private SettingsHolder(Camera camera, Color markerColor, CameraHolder cameraHolder, ImageRecognition imageRecognition,
                           Color colorRangeStart, Color colorRangeEnd, String haarCascadeConfigFilename) {
        this.cameraHolder = cameraHolder;
        this.imageRecognition = imageRecognition;
        this.camera = camera;
        this.markerColorProperty = new ObjectPropertyBase<Color>(markerColor) {
            @Override
            public void set(Color newValue) {
                Color oldValue = get();
                super.set(newValue);
                log.info(String.format("%s value was changed from ", getName()) +
                        String.format("H:%1.2f S:%1.2f B:%1.2f", oldValue.getHue(), oldValue.getSaturation(), oldValue.getBrightness()) +
                        String.format(" to H:%1.2f S:%1.2f B:%1.2f", newValue.getHue(), newValue.getSaturation(), newValue.getBrightness()));
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "Marker color";
            }
        };
        this.colorRangeStartProperty = new ObjectPropertyBase<Color>(colorRangeStart) {
            @Override
            public void set(Color newValue) {
                Color oldValue = get();
                super.set(newValue);
                log.info(String.format("%s value was changed from ", getName()) +
                        String.format("H:%1.2f S:%1.2f B:%1.2f", oldValue.getHue(), oldValue.getSaturation(), oldValue.getBrightness()) +
                        String.format(" to H:%1.2f S:%1.2f B:%1.2f", newValue.getHue(), newValue.getSaturation(), newValue.getBrightness()));
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "Image recognition color range start";
            }
        };
        this.colorRangeEndProperty = new ObjectPropertyBase<Color>(colorRangeEnd) {
            @Override
            public void set(Color newValue) {
                Color oldValue = get();
                super.set(newValue);
                log.info(String.format("%s color was changed from ", getName()) +
                        String.format("H:%1.2f S:%1.2f B:%1.2f", oldValue.getHue(), oldValue.getSaturation(), oldValue.getBrightness()) +
                        String.format(" to H:%1.2f S:%1.2f B:%1.2f", newValue.getHue(), newValue.getSaturation(), newValue.getBrightness()));
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "Image recognition color range end";
            }
        };
        this.haarCascadeConfigFilename = haarCascadeConfigFilename;
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
    /* Property Getters */

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
    /* Getters and setters */
}