package ru.vsu.cvprocessing.recognition;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.vsu.cvprocessing.event.SendingDataEvent;
import ru.vsu.cvprocessing.event.SendingDataPublisher;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.vsu.cvprocessing.recognition.ImageRecognitionMethod.*;
import static ru.vsu.cvprocessing.recognition.OpenCVUtils.matToImage;
import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

public class RecognizeByCascade extends ImageRecognition {
    private SendingDataPublisher sendingDataPublisher = getInstance().getApplicationContext().getBean(SendingDataPublisher.class);

    private int absoluteFaceSize;
    private CascadeClassifier cascadeClassifier;

    public RecognizeByCascade(String filePath) {
        super();
        imageRecognitionMethod = BYCASCADE;
        absoluteFaceSize = 0;
        cascadeClassifier = new CascadeClassifier(filePath);
    }

    @Override
    public void openVideoCapture(Map<String, Object> parameters) throws Exception {
        checkNotNull(camera, "Camera required");
        ImageView viewCamera = checkNotNull((ImageView) parameters.get("viewCamera"),
                "Camera's ImageView required");

        videoCapture.open(camera.getWebcamIndex());
        if (videoCapture.isOpened()) {
            // grab a frame every 33 ms (30 frames/sec)
            Runnable frameGrabber = () -> {
                Image image = grabFrame(new HashMap<>());
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
    Image grabFrame(Map<String, Object> parameters) {
        Image image = null;

        Mat frame = new Mat();
        videoCapture.read(frame);

        if (!frame.empty()) {
            objectDetected = findFaces(frame);
            image = matToImage(frame);
            sendingDataPublisher.publish(new SendingDataEvent(objectDetected, false, -(xCoordinate - prevXCoordinate)));
            sendingDataPublisher.publish(new SendingDataEvent(objectDetected, true, -(yCoordinate - prevYCoordinate)));
        }

        return image;
    }

    private boolean findFaces(Mat frame) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        cascadeClassifier.detectMultiScale(grayFrame, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        if (faces.elemSize() == 0) {
            return false;
        }
        savePrevCoordinate();
        Rect firstFace = faces.toList().get(0);
        xCoordinate = (int) (firstFace.br().x - (firstFace.br().x - firstFace.tl().x) / 2);
        yCoordinate = (int) (firstFace.br().y - (firstFace.br().y - firstFace.tl().y) / 2);
        drawRectangles(faces.toList(), frame, new Scalar(0, 255, 0), new Scalar(255, 0, 0));

        return true;
    }

    private Mat drawRectangles(List<Rect> rects, Mat frame, Scalar mainColor, Scalar sideColor) {
        Imgproc.rectangle(frame, rects.get(0).tl(), rects.get(0).br(), mainColor, 3);
        for (int i = 1; i < rects.size(); i++) {
            Imgproc.rectangle(frame, rects.get(i).tl(), rects.get(i).br(), sideColor, 3);
        }
        Imgproc.drawMarker(frame, new Point(xCoordinate, yCoordinate),
                new Scalar(markerColor.getBlue() * 255, markerColor.getGreen() * 255, markerColor.getRed() * 255),
                0, 50, 2, 4);
        return frame;
    }
}
