package imageRecognition;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import observer.Observer;
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

import static imageRecognition.OpenCVUtils.matToImage;

public class RecognizeByFace extends ImageRecognition {
    private List<Observer> observers = new ArrayList<>();
    private ScheduledExecutorService timer;

    private int absoluteFaceSize;
    private CascadeClassifier cascadeClassifier;

    public RecognizeByFace() {
        absoluteFaceSize = 0;
        cascadeClassifier = new CascadeClassifier();
        cascadeClassifier.load("/Users/eugene.home/IdeaProjects/CVProcessing/src/main/resources/haarcascades/haarcascade_frontalface_alt.xml");
}

    @Override
    public void openVideoCapture(Map<String, Object> parameters) throws Exception {
        ImageView viewCamera = (ImageView) parameters.get("viewCamera");

        videoCapture.open(webCameraIndex);
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
            throw new Exception("Can't open camera with index " + webCameraIndex + ".");
        }
    }

    @Override
    public void closeVideoCapture() {
        if (timer != null && !timer.isShutdown()) {
            this.timer.shutdown();
            try {
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.closeVideoCapture();
    }

    @Override
    Image grabFrame(Map<String, Object> parameters) {
        Image image = null;

        Mat frame = new Mat();
        videoCapture.read(frame);

        if (!frame.empty()) {
            findFaces(frame);
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
                new Size(absoluteFaceSize,absoluteFaceSize), new Size());

        if (faces.elemSize() == 0) {
            return false;
        }
        Rect firstFace = faces.toList().get(0);
        xCoordinate = (int) (firstFace.br().x - (firstFace.br().x - firstFace.tl().x) / 2);
        yCoordinate = (int) (firstFace.br().y - (firstFace.br().y - firstFace.tl().y) / 2);
        drawRectangles(faces.toList(), frame, new Scalar(0, 255, 0));
        return true;
    }

    private Mat drawRectangles(List<Rect> rects, Mat frame, Scalar color) {
        for (Rect rect : rects) {
            Imgproc.rectangle(frame, rect.tl(), rect.br(), color, 3);
        }
        // TODO customize marker
        Imgproc.drawMarker(frame, new Point(xCoordinate, yCoordinate), new Scalar(0, 0, 255),
                0, 50, 2, 4);
        return frame;
    }

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
        observers.forEach(observer -> observer.update(xCoordinate - prevXCoordinate,yCoordinate - prevYCoordinate));
    }
}
