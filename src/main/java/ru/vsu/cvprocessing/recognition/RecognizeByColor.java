package ru.vsu.cvprocessing.recognition;

import javafx.scene.paint.Color;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.vsu.cvprocessing.event.SendingDataEvent;
import ru.vsu.cvprocessing.event.SendingDataPublisher;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.vsu.cvprocessing.recognition.ImageRecognitionMethod.*;
import static ru.vsu.cvprocessing.recognition.OpenCVUtils.matToImage;
import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

public class RecognizeByColor extends ImageRecognition {
    private SendingDataPublisher sendingDataPublisher = getInstance().getApplicationContext().getBean(SendingDataPublisher.class);

    public RecognizeByColor() {
        super();
        imageRecognitionMethod = BYCOLOR;
    }

    @Override
    public void openVideoCapture(Map<String, Object> parameters) throws Exception {
        checkNotNull(camera, "Camera required");
        ImageView viewCamera = checkNotNull((ImageView) parameters.get("viewCamera"),
                "Camera's ImageView required");
        ImageView viewMaskImage = checkNotNull((ImageView) parameters.get("viewMaskImage"),
                "Mask's ImageView required");
        ImageView viewMorphImage = checkNotNull((ImageView) parameters.get("viewMorphImage"),
                "Morph's ImageView required");
        Color colorRangeStart = checkNotNull((Color) parameters.get("colorRangeStart"),
                "Color range start required");
        Color colorRangeEnd = checkNotNull((Color) parameters.get("colorRangeEnd"),
                "Color range end required");

        videoCapture.open(camera.getWebcamIndex());
        if (videoCapture.isOpened()) {
            // grab a frame every 33 ms (30 frames/sec)
            Runnable frameGrabber = () -> {
                Image image = grabFrame(new HashMap<String, Object>() {{
                    put("lowerb", colorToOpenCVHSB(colorRangeStart));
                    put("upperb", colorToOpenCVHSB(colorRangeEnd));
                    put("viewMaskImage", viewMaskImage);
                    put("viewMorphImage", viewMorphImage);
                }});
                viewCamera.setImage(image);
            };
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MICROSECONDS);
//            setRefreshPrevCoordinateFrequency(5);
        } else {
            throw new Exception("Can't open camera with index " + camera.getWebcamIndex() + ".");
        }
    }

    private Scalar colorToOpenCVHSB(Color color) {
        return new Scalar(color.getHue() * 256d / 360d, color.getSaturation() * 256d, color.getBrightness() * 256d);
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
            if (viewMaskImage != null) {
                Platform.runLater(() -> viewMaskImage.imageProperty().set(matToImage(maskImage)));
            }

            // dilate with large element, erode with small ones
            Mat morphImage = new Mat();

            Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
            Imgproc.dilate(maskImage, morphImage, dilateElement);

            Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
            Imgproc.erode(maskImage, morphImage, erodeElement);

            if (viewMorphImage != null) {
                Platform.runLater(() -> viewMorphImage.imageProperty().set(matToImage(maskImage)));
            }

            Mat hierarchy = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            objectDetected = findContours(maskImage, hierarchy, contours);
            if (objectDetected) {
                frame = drawContours(hierarchy, contours, frame, new Scalar(255, 0, 0));
            }
            // convert the Mat object (OpenCV) to Image (JavaFX)
            image = matToImage(frame);
        }

        return image;
    }

    private boolean findContours(Mat image, Mat hierarchy, List<MatOfPoint> contours) {
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Point> pointList = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Moments moments = Imgproc.moments(contour, false);
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
        savePrevCoordinate();

        xCoordinate = (int) Math.round(averagePoint.x / validPointCount);
        yCoordinate = (int) Math.round(averagePoint.y / validPointCount);

        publishCoordinates();
//        sendingDataPublisher.publish(new SendingDataEvent(objectDetected, false,
//                (int) Math.round((double) -(xCoordinate - prevXCoordinate) / (camera.getWidth() / camera.getFieldOfView()))));
//        sendingDataPublisher.publish(new SendingDataEvent(objectDetected, true,
//                (int) Math.round((double) -(yCoordinate - prevYCoordinate) / (camera.getWidth() / camera.getFieldOfView()))));

        return hierarchy.size().height > 0 && hierarchy.size().width > 0;
    }

    private Mat drawContours(Mat hierarchy, List<MatOfPoint> contours, Mat frame, Scalar color) {
        for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
            Imgproc.drawContours(frame, contours, idx, color);
        }
        Imgproc.drawMarker(frame, new Point(xCoordinate, yCoordinate),
                new Scalar(markerColor.getBlue() * 255, markerColor.getGreen() * 255, markerColor.getRed() * 255),
                0, 50, 2, 4);
        return frame;
    }
}