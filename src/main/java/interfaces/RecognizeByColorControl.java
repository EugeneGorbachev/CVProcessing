package interfaces;

import controllers.Utils;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RecognizeByColorControl extends ImageRecognition {
    private ScheduledExecutorService timer;

    @Override
    public void openVideoCapture(Map<String, Object> parameters) throws Exception {
        Slider hueRangeStartSlider = (Slider) parameters.get("hueRangeStartSlider");
        Slider saturationRangeStartSlider = (Slider) parameters.get("saturationRangeStartSlider");
        Slider brightnessRangeStartSlider = (Slider) parameters.get("brightnessRangeStartSlider");
        Slider hueRangeEndSlider = (Slider) parameters.get("hueRangeEndSlider");
        Slider saturationRangeEndSlider = (Slider) parameters.get("saturationRangeEndSlider");
        Slider brightnessRangeEndSlider = (Slider) parameters.get("brightnessRangeEndSlider");

        ImageView viewCamera = (ImageView) parameters.get("viewCamera");
        ImageView viewMaskImage = (ImageView) parameters.get("viewMaskImage");
        ImageView viewMorphImage = (ImageView) parameters.get("viewMorphImage");

        videoCapture.open(webCameraIndex);
        if (videoCapture.isOpened()) {
            // grab a frame every 33 ms (30 frames/sec)
            Runnable frameGrabber = () -> {
                Image image = grabFrame(new HashMap<String, Object>() {{
                    put("lowerb", HSBColorModelToOpenCVHSB(hueRangeStartSlider, saturationRangeStartSlider, brightnessRangeStartSlider));
                    put("upperb", HSBColorModelToOpenCVHSB(hueRangeEndSlider,saturationRangeEndSlider, brightnessRangeEndSlider));
                    put("viewMaskImage", viewMaskImage);
                    put("viewMorphImage", viewMorphImage);
                }});
                viewCamera.setImage(image);
            };
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MICROSECONDS);
        } else {
            throw new Exception("Can't open camera with index " + webCameraIndex + ".");
        }
    }

    @Override
    public void closeVideoCapture() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try {
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.closeVideoCapture();
    }

    private Scalar HSBColorModelToOpenCVHSB(Slider hueSlider, Slider saturationSlider, Slider brightnessSlider) {
        return new Scalar(hueSlider.getValue() * 0.5d, saturationSlider.getValue() * 2.56d,
                brightnessSlider.getValue() * 2.56d);
    }

    @Override
    Image grabFrame(Map<String, Object> parameters) {
        Scalar lowerb = (Scalar) parameters.get("lowerb");
        Scalar upperb = (Scalar) parameters.get("upperb");
        ImageView viewMaskImage = (ImageView) parameters.get("viewMaskImage");
        ImageView viewMorphImage = (ImageView) parameters.get("viewMorphImage");

        Image image = null;

        // The class Mat represents an n-dimensional dense numerical single-channel or multi-channel array.
        Mat frame = new Mat();
        videoCapture.read(frame);

        if (!frame.empty()) {
            Mat maskImage = new Mat();
            Imgproc.blur(frame, maskImage, new Size(7, 7));// remove some noise
            Imgproc.cvtColor(maskImage, maskImage, Imgproc.COLOR_BGR2HSV);// convert to HSV (also HSB)

            Core.inRange(maskImage, lowerb, upperb, maskImage);
            Utils.onFXThread(viewMaskImage.imageProperty(), Utils.mat2Image(maskImage));// todo deal with Utils

            // morphological operators TODO remain only mask
            // dilate with large element, erode with small ones
            Mat morphImage = new Mat();

            Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
            Imgproc.dilate(maskImage, morphImage, dilateElement);

            Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
            Imgproc.erode(maskImage, morphImage, erodeElement);

            Utils.onFXThread(viewMorphImage.imageProperty(), Utils.mat2Image(morphImage));// todo deal with Utils

            frame = Utils.findAndDrawContours(maskImage, frame);// todo deal with Utils
            // convert the Mat object (OpenCV) to Image (JavaFX)
            image = Utils.mat2Image(frame);// todo deal with Utils
        }

        return image;
    }
}
