package ru.vsu.cvprocessing.recognition;

import javafx.scene.paint.Color;
import ru.vsu.cvprocessing.event.SendingDataEvent;
import ru.vsu.cvprocessing.event.SendingDataPublisher;
import ru.vsu.cvprocessing.holder.Camera;
import javafx.application.Platform;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.vsu.cvprocessing.observer.Observer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.vsu.cvprocessing.recognition.ImageRecognitionMethod.*;
import static ru.vsu.cvprocessing.recognition.OpenCVUtils.matToImage;
import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

public class RecognizeByColor extends ImageRecognition {
    private List<Observer> observers = new ArrayList<>();
    private ScheduledExecutorService timer;
    private SendingDataPublisher sendingDataPublisher;

    public RecognizeByColor(Camera camera) {
        super(camera);
        imageRecognitionMethod = BYCOLOR;
    }

    @Override
    public void openVideoCapture(Map<String, Object> parameters) throws Exception {
        ImageView viewCamera = (ImageView) parameters.get("viewCamera");
        ImageView viewMaskImage = (ImageView) parameters.get("viewMaskImage");
        ImageView viewMorphImage = (ImageView) parameters.get("viewMorphImage");

        videoCapture.open(camera.getWebcamIndex());
        if (videoCapture.isOpened()) {
            // grab a frame every 33 ms (30 frames/sec)
            Runnable frameGrabber = () -> {
                Image image = grabFrame(new HashMap<String, Object>() {{
                    put("lowerb", colorToOpenCVHSB(getInstance().getColorRangeStart()));
                    put("upperb", colorToOpenCVHSB(getInstance().getColorRangeEnd()));
                    put("viewMaskImage", viewMaskImage);
                    put("viewMorphImage", viewMorphImage);
                }});
                viewCamera.setImage(image);
            };
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MICROSECONDS);
            setRefreshPrevCoordinateFrequency(5);
        } else {
            throw new Exception("Can't open camera with index " + camera.getWebcamIndex() + ".");
        }
    }

    @Override
    public boolean closeVideoCapture() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try {
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return super.closeVideoCapture();
    }

    // TODO remove
    private Scalar colorToOpenCVHSB(Slider hueSlider, Slider saturationSlider, Slider brightnessSlider) {
        return new Scalar(hueSlider.getValue() * 0.5d, saturationSlider.getValue() * 2.56d,
                brightnessSlider.getValue() * 2.56d);
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
        // TODO somehow call notifyObservers after savePrevCoordinate
        savePrevCoordinate();
        xCoordinate = (int) Math.round(averagePoint.x / validPointCount);
        yCoordinate = (int) Math.round(averagePoint.y / validPointCount);
//        sendingDataPublisher.publish(new SendingDataEvent());
        notifyObservers();

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

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void notifyObservers() {
        observers.forEach(observer -> observer.update(objectDetected,
                -(xCoordinate - prevXCoordinate), -(yCoordinate - prevYCoordinate))
        );
    }
}