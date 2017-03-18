package ru.vsu.cvprocessing.recognition;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.vsu.cvprocessing.recognition.ImageRecognitionMethod.*;

public class FakeImageRecognition extends ImageRecognition {
    public FakeImageRecognition() {
        super();
        imageRecognitionMethod = FAKE;
    }

    /* Open video capture method implementation */
    @Override
    public void openVideoCapture(Map<String, Object> parameters) throws Exception {
        checkNotNull(camera, "Camera required");
        ImageView viewCamera = checkNotNull((ImageView) parameters.get("viewCamera"),
                "Camera's ImageView required");

        videoCapture.open(camera.getWebCamIndex());

        videoCapture.set(3, camera.getWidth());
        videoCapture.set(4, camera.getHeight());

        Runnable frameGrabber = () -> viewCamera.setImage(grabFrame(new HashMap<>()));
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MICROSECONDS);
    }

    /* Accessory method */
    @Override
    protected Image grabFrame(Map<String, Object> parameters) {
        Mat frame = new Mat();
        videoCapture.read(frame);
        return matToImage(frame);
    }
}
