package controllers;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public final class Utils {
    public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> property.set(value));
    }

    /* Setting properties */
    public static void imageViewDimension(ImageView imageView, int width) {
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(width);
    }

    // Without preserve ratio TODO delete by useless
    public static void imageViewDimension(ImageView imageView, int height, int width) {
        imageView.setPreserveRatio(false);
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
    }
    /* Setting properties */

    public static Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public static Mat findAndDrawContours(Mat maskImage, Mat frame) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // find contours
        Imgproc.findContours(maskImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Moments> mu = new ArrayList<Moments>(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            mu.add(i, Imgproc.moments(contours.get(i), false));
            Moments p = mu.get(i);
            int x = (int) (p.get_m10() / p.get_m00());
            int y = (int) (p.get_m01() / p.get_m00());
            Imgproc.drawMarker(frame, new Point(x, y), new Scalar(49, 0, 255));
        }

        // if any contour exist
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
            // for each contour, display it in blue
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
                Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
            }
        }

        return frame;
    }
}