package ru.vsu.cvprocessing.recognition;

import ru.vsu.cvprocessing.holder.Camera;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.vsu.cvprocessing.observer.Observer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.vsu.cvprocessing.recognition.ImageRecognitionMethod.*;
import static ru.vsu.cvprocessing.recognition.OpenCVUtils.matToImage;

public class RecognizeByCascade extends ImageRecognition {
    private List<Observer> observers = new ArrayList<>();
    private ScheduledExecutorService timer;

    private int absoluteFaceSize;
    private CascadeClassifier cascadeClassifier;

    public RecognizeByCascade(Camera camera, String filePath) {
        super(camera);
        imageRecognitionMethod = BYCASCADE;
        absoluteFaceSize = 0;
        cascadeClassifier = new CascadeClassifier(filePath);
    }

    @Override
    public void openVideoCapture(Map<String, Object> parameters) throws Exception {
        ImageView viewCamera = (ImageView) parameters.get("viewCamera");

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
    public boolean closeVideoCapture() {
        if (timer != null && !timer.isShutdown()) {
            this.timer.shutdown();
            try {
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return super.closeVideoCapture();
    }

    @Override
    Image grabFrame(Map<String, Object> parameters) {
        Image image = null;

        Mat frame = new Mat();
        videoCapture.read(frame);

        if (!frame.empty()) {
            objectDetected = findFaces(frame);
            image = matToImage(frame);
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
            notifyObservers();
            return false;
        }
        // TODO somehow call notifyObservers after savePrevCoordinate
        savePrevCoordinate();
        Rect firstFace = faces.toList().get(0);
        xCoordinate = (int) (firstFace.br().x - (firstFace.br().x - firstFace.tl().x) / 2);
        yCoordinate = (int) (firstFace.br().y - (firstFace.br().y - firstFace.tl().y) / 2);
        drawRectangles(faces.toList(), frame, new Scalar(0, 255, 0), new Scalar(255, 0, 0));
        notifyObservers();

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

    // TODO replace with events
    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        observers.forEach(observer -> observer.update(objectDetected,
                -(xCoordinate - prevXCoordinate), -(yCoordinate - prevYCoordinate))
        );
    }
}
