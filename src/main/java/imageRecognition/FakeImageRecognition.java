package imageRecognition;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import observer.Observer;
import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static imageRecognition.OpenCVUtils.matToImage;

public class FakeImageRecognition extends ImageRecognition {
    private ScheduledExecutorService timer;

    @Override
    public void addObserver(Observer o) {

    }

    @Override
    public void removeObserver(Observer o) {

    }

    @Override
    public void notifyObservers() {

    }

    @Override
    public void openVideoCapture(Map<String, Object> parameters) throws Exception {
        ImageView viewCamera = (ImageView) parameters.get("viewCamera");

        videoCapture.open(webCameraIndex);

        Runnable frameGrabber = () -> {
            viewCamera.setImage(grabFrame(new HashMap<>()));
        };
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MICROSECONDS);
    }

    @Override
    public void closeVideoCapture() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try {
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.closeVideoCapture();
    }

    @Override
    Image grabFrame(Map<String, Object> parameters) {
        Mat frame = new Mat();
        videoCapture.read(frame);
        return matToImage(frame);
    }
}
