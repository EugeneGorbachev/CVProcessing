package interfaces;

import javafx.application.Platform;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.ByteArrayInputStream;
import java.util.*;
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
                    put("upperb", HSBColorModelToOpenCVHSB(hueRangeEndSlider, saturationRangeEndSlider, brightnessRangeEndSlider));
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
            Platform.runLater(() -> viewMaskImage.imageProperty().set(mat2Image(maskImage)));

            // dilate with large element, erode with small ones
            Mat morphImage = new Mat();

            Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
            Imgproc.dilate(maskImage, morphImage, dilateElement);

            Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
            Imgproc.erode(maskImage, morphImage, erodeElement);

            Platform.runLater(() -> viewMorphImage.imageProperty().set(mat2Image(maskImage)));

            Mat hierarchy = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            if (findContours(maskImage, frame, hierarchy, contours)) {
                frame = drawContours(hierarchy, contours, frame, new Scalar(255, 0, 0));
            }
            // convert the Mat object (OpenCV) to Image (JavaFX)
            image = mat2Image(frame);
        }

        return image;
    }

    private boolean findContours(Mat image, Mat frame, Mat hierarchy, List<MatOfPoint> contours) {
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Point> pointList = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {
            Moments moments = Imgproc.moments(contours.get(i), false);
            double x = moments.get_m10() / moments.get_m00();
            double y = moments.get_m01() / moments.get_m00();
            pointList.add(new Point(x, y));
        }

        Point averagePoint = new Point();
        int validPointCount = 0;
        for (Point point : pointList) {
            if (!Double.isNaN(point.x) && !Double.isNaN(point.y)
                    && Double.isFinite(point.x) && Double.isFinite(point.y)) {
                averagePoint.x += point.x;
                averagePoint.y += point.y;
                validPointCount++;
            }
        }
        xCoordinate = (int) Math.round(averagePoint.x / validPointCount);
        yCoordinate = (int) Math.round(averagePoint.y / validPointCount);

        return hierarchy.size().height > 0 && hierarchy.size().width > 0;
    }

    private Mat drawContours(Mat hierarchy, List<MatOfPoint> contours, Mat frame, Scalar color) {
        for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
            Imgproc.drawContours(frame, contours, idx, color);
        }
        // TODO customize marker
        Imgproc.drawMarker(frame, new Point(xCoordinate, yCoordinate), new Scalar(0, 0, 255),
                0, 50, 2, 4);
        return frame;
    }

    /* Static methods */
    public static Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
    /* Static methods */
}
