package controllers;

import interfaces.CameraHolder;
import interfaces.ServoMotorControl;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CVFormController implements Initializable {
    /* Central part*/
    @FXML
    private ImageView viewCamera;
    /*Central part*/

    /* Recognize by color pane */
    @FXML
    private ImageView viewMaskImage;
    @FXML
    private ImageView viewMorphImage;

    @FXML
    private Pane paneRangeStartColor;
    @FXML
    private Pane paneRangeEndColor;

    @FXML
    private TextField hueRangeStartValue;
    @FXML
    private TextField saturationRangeStartValue;
    @FXML
    private TextField brightnessRangeStartValue;
    @FXML
    private TextField hueRangeEndValue;
    @FXML
    private TextField saturationRangeEndValue;
    @FXML
    private TextField brightnessRangeEndValue;

    @FXML
    private Slider hueRangeStartSlider;
    @FXML
    private Slider saturationRangeStartSlider;
    @FXML
    private Slider brightnessRangeStartSlider;
    @FXML
    private Slider hueRangeEndSlider;
    @FXML
    private Slider saturationRangeEndSlider;
    @FXML
    private Slider brightnessRangeEndSlider;
    /* Recognize by color pane */

    /* Servo connection settings */
    @FXML
    private ChoiceBox OSChooseBox;
    @FXML
    private ChoiceBox COMPortChooseBox;
    @FXML
    private Button testConnectionButton;
    @FXML
    private Slider servoAngleSlider;
    @FXML
    private TextArea logTextArea;
    /* Servo connection settings */

    public CameraHolder cameraHolder;

    private final String[] WindowsPortNames = {"COM4"};
    private final String[] LinuxPortNames = {"/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyACM0"};
    private final String[] MacOSPortNames = {"COM4"};

    private ScheduledExecutorService timer;
    private VideoCapture videoCapture = new VideoCapture();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
         cameraHolder = new ServoMotorControl();

        /* Initialize servo settings */
        OSChooseBox.setItems(FXCollections.observableArrayList(OperatingSystem.values()));
        OSChooseBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            switch ((OperatingSystem) OSChooseBox.getSelectionModel().getSelectedItem()) {
                case WINDOWS:
                    COMPortChooseBox.setItems(FXCollections.observableArrayList(WindowsPortNames));
                    break;
                case LINUX:
                    COMPortChooseBox.setItems(FXCollections.observableArrayList(LinuxPortNames));
                    break;
                case MACOS:
                    COMPortChooseBox.setItems(FXCollections.observableArrayList(MacOSPortNames));
                    break;
            }
            COMPortChooseBox.setDisable(false);
        });
        COMPortChooseBox.valueProperty().addListener(((observable, oldValue, newValue) ->
                tryConnectSerialPort((String) newValue)));
        /* Initialize servo settings */

        /* Binding slider and textfield value */
        IntegerProperty hueRangeStartInteger = new SimpleIntegerProperty(0);
        hueRangeStartSlider.valueProperty().bindBidirectional(hueRangeStartInteger);
        hueRangeStartValue.textProperty().bind(hueRangeStartInteger.asString());
        IntegerProperty saturationRangeStartInteger = new SimpleIntegerProperty(0);
        saturationRangeStartSlider.valueProperty().bindBidirectional(saturationRangeStartInteger);
        saturationRangeStartValue.textProperty().bind(saturationRangeStartInteger.asString());
        IntegerProperty brightnessRangeStartInteger = new SimpleIntegerProperty(0);
        brightnessRangeStartSlider.valueProperty().bindBidirectional(brightnessRangeStartInteger);
        brightnessRangeStartValue.textProperty().bind(brightnessRangeStartInteger.asString());

        IntegerProperty hueRangeEndInteger = new SimpleIntegerProperty(0);
        hueRangeEndSlider.valueProperty().bindBidirectional(hueRangeEndInteger);
        hueRangeEndValue.textProperty().bind(hueRangeEndInteger.asString());
        IntegerProperty saturationRangeEndInteger = new SimpleIntegerProperty(0);
        saturationRangeEndSlider.valueProperty().bindBidirectional(saturationRangeEndInteger);
        saturationRangeEndValue.textProperty().bind(saturationRangeEndInteger.asString());
        IntegerProperty brightnessRangeEndInteger = new SimpleIntegerProperty(0);
        brightnessRangeEndSlider.valueProperty().bindBidirectional(brightnessRangeEndInteger);
        brightnessRangeEndValue.textProperty().bind(brightnessRangeEndInteger.asString());

        hueRangeStartSlider.valueProperty().addListener((observable, oldValue, newValue) -> handleChangeStartRangeColor());
        saturationRangeStartSlider.valueProperty().addListener((observable, oldValue, newValue) -> handleChangeStartRangeColor());
        brightnessRangeStartSlider.valueProperty().addListener((observable, oldValue, newValue) -> handleChangeStartRangeColor());
        hueRangeEndSlider.valueProperty().addListener((observable, oldValue, newValue) -> handleChangeEndRangeColor());
        saturationRangeEndSlider.valueProperty().addListener(((observable, oldValue, newValue) -> handleChangeEndRangeColor()));
        brightnessRangeEndSlider.valueProperty().addListener(((observable, oldValue, newValue) -> handleChangeEndRangeColor()));
        /* Binding slider and textfield value */

        /* Initialize views and start video capture */
        Utils.imageViewDimension(viewCamera, 600);
        Utils.imageViewDimension(viewMaskImage, 400);
        Utils.imageViewDimension(viewMorphImage, 400);

        /* make a selector for different cameras instead 0 */
        videoCapture.open(0);
        if (videoCapture.isOpened()) {
            // grab a frame every 33 ms (30 frames/sec)
            Runnable frameGrabber = () -> {
                Scalar lowerb = new Scalar(hueRangeStartSlider.getValue() * 0.5d, saturationRangeStartSlider.getValue() * 2.56d,
                        brightnessRangeStartSlider.getValue() * 2.56d);
                Scalar upperb = new Scalar(hueRangeEndSlider.getValue() * 0.5d, saturationRangeEndSlider.getValue() * 2.56d,
                        brightnessRangeEndSlider.getValue() * 2.56d);
                Image image = grabFrame(lowerb, upperb);
                viewCamera.setImage(image);
            };
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MICROSECONDS);
        } else {
            System.err.println("Can't open camera");
        }
        /* Initialize views and start video capture */

        handleChangeStartRangeColor();
        handleChangeEndRangeColor();
    }

    private void logMessage(String message) {
        Platform.runLater(() -> logTextArea.appendText(new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()) + " - " + message));
    }

    private void tryConnectSerialPort(String portName) {
        try {
            cameraHolder.setUpConnection(new HashMap<String,Object>() {{
                put("portName", portName);
            }});
            servoAngleSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                    cameraHolder.setHorizontalAngle((int) servoAngleSlider.getValue()));
            testConnectionButton.setDisable(false);
            servoAngleSlider.setDisable(false);
        } catch (Exception e) {
            logMessage(e.getMessage());
            testConnectionButton.setDisable(true);
            servoAngleSlider.setDisable(true);
        }
    }

    private Image grabFrame(Scalar lowerb, Scalar upperb) {
        Image image = null;

//      The class Mat represents an n-dimensional dense numerical single-channel or multi-channel array.
        Mat frame = new Mat();

        videoCapture.read(frame);
        if (!frame.empty()) {
            Mat maskImage = new Mat();
            Imgproc.blur(frame, maskImage, new Size(7, 7));// remove some noise
            Imgproc.cvtColor(maskImage, maskImage, Imgproc.COLOR_BGR2HSV);// convert to HSV

            Core.inRange(maskImage, lowerb, upperb, maskImage);
            Utils.onFXThread(this.viewMaskImage.imageProperty(), Utils.mat2Image(maskImage));

            // morphological operators
            // dilate with large element, erode with small ones
            Mat morphImage = new Mat();

            Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
            Imgproc.dilate(maskImage, morphImage, dilateElement);

            Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
            Imgproc.erode(maskImage, morphImage, erodeElement);

            Utils.onFXThread(viewMorphImage.imageProperty(), Utils.mat2Image(morphImage));

            frame = Utils.findAndDrawBalls(morphImage, frame);
            // convert the Mat object (OpenCV) to Image (JavaFX)
            image = Utils.mat2Image(frame);
        }

        return image;
    }

    private void handleChangeStartRangeColor() {
        double hue = hueRangeStartSlider.getValue(),
                saturation = saturationRangeStartSlider.getValue() * 0.01d,
                brightness = brightnessRangeStartSlider.getValue() * 0.01d;
        Color selectedColor = Color.hsb(hue, saturation, brightness);

        Background background = new Background(new BackgroundFill(selectedColor, CornerRadii.EMPTY, Insets.EMPTY));
        paneRangeStartColor.setBackground(background);
    }

    private void handleChangeEndRangeColor() {
        double hue = hueRangeEndSlider.getValue(),
                saturation = saturationRangeEndSlider.getValue() * 0.01d,
                brightness = brightnessRangeEndSlider.getValue() * 0.01d;
        Color selectedColor = Color.hsb(hue, saturation, brightness);

        Background background = new Background(new BackgroundFill(selectedColor, CornerRadii.EMPTY, Insets.EMPTY));
        paneRangeEndColor.setBackground(background);
    }

    public void handleClose() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try {
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (videoCapture.isOpened()) {
            videoCapture.release();
        }
    }
}
