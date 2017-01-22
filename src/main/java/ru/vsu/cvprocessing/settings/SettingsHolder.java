package ru.vsu.cvprocessing.settings;

import ru.vsu.cvprocessing.holder.Camera;
import ru.vsu.cvprocessing.holder.CameraHolder;
import ru.vsu.cvprocessing.holder.ServoMotorControl;
import ru.vsu.cvprocessing.recognition.FakeImageRecognition;
import ru.vsu.cvprocessing.recognition.ImageRecognition;
import ru.vsu.cvprocessing.recognition.RecognizeByColor;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.HashMap;

public class SettingsHolder {
    /* Singleton */
    private static SettingsHolder instance = null;

    public static SettingsHolder getInstance() {
        if (instance == null) {
            Camera camera = new Camera(0, 70, 233.3d, 400d);
            instance = new SettingsHolder(
                    new ServoMotorControl(camera),
                    new FakeImageRecognition(camera),
                    0,
                    new Color(1, 0, 0, 1),
                    new HSBRange(0, 0,
                            0, 0,
                            0, 0),
                    null
            );
        }
        return instance;
    }
    /* Singleton */

    private CameraHolder cameraHolder;
    private ImageRecognition imageRecognition;

    /* Image preferences */
    private int webcameraIndex;
    private Color markerColor;
    /* Image preferences */

    /* Recognize by color preferences */
    private HSBRange hsbRange;
    /* Recognize by color preferences */

    /* Recognize by cascade preferences */
    private String haarcascade;
    /* Recognize by cascade preferences */

    public void switchToFake(Camera camera, ImageView viewCamera) {
        imageRecognition.closeVideoCapture();
        imageRecognition = new FakeImageRecognition(camera);

        try {
            imageRecognition.openVideoCapture(new HashMap<String, Object>() {{
                put("viewCamera", viewCamera);
            }});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchToRecognizeByColor(Camera camera, ImageView viewCamera, ImageView viewMaskImage, ImageView viewMorphImage) {
        imageRecognition.closeVideoCapture();
        imageRecognition = new RecognizeByColor(camera);
        imageRecognition.addObserver(cameraHolder);

        try {
            imageRecognition.openVideoCapture(new HashMap<String, Object>() {{
                put("viewCamera", viewCamera);
                put("viewMaskImage", viewMaskImage);
                put("viewMorphImage", viewMorphImage);
            }});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO realise builder
    private SettingsHolder(CameraHolder cameraHolder, ImageRecognition imageRecognition, int webcameraIndex,
                           Color markerColor, HSBRange hsbRange, String haarcascade) {
        this.cameraHolder = cameraHolder;
        this.imageRecognition = imageRecognition;
        this.webcameraIndex = webcameraIndex;
        this.markerColor = markerColor;
        this.hsbRange = hsbRange;
        this.haarcascade = haarcascade;
    }

    public CameraHolder getCameraHolder() {
        return cameraHolder;
    }

    public ImageRecognition getImageRecognition() {
        return imageRecognition;
    }

    public int getWebcameraIndex() {
        return webcameraIndex;
    }

    public Color getMarkerColor() {
        return markerColor;
    }

    public HSBRange getHsbRange() {
        return hsbRange;
    }

    public String getHaarcascade() {
        return haarcascade;
    }
}