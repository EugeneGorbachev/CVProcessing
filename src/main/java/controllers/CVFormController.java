package controllers;

import cameraHolder.CameraHolder;
import imageRecognition.ImageRecognition;
import imageRecognition.RecognizeByColor;
import cameraHolder.ServoMotorControl;
import imageRecognition.RecognizeByFace;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ResourceBundle;

public class CVFormController implements Initializable {
    enum OperatingSystem {
        WINDOWS, LINUX, MACOS
    }

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
    private Button establishConnectionButton;
    @FXML
    private Button closeConnectionButton;
    @FXML
    private Slider servoAngleSlider;
    @FXML
    private TextArea logTextArea;
    /* Servo connection settings */


    private final String[] WindowsPortNames = {"COM4", "COM6", "COM7", "COM8"};
    private final String[] LinuxPortNames = {"/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyACM0"};
    private final String[] MacOSPortNames = {"/dev/tty.wchusbserial1420"};

    private CameraHolder cameraHolder;
    private ImageRecognition imageRecognition;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cameraHolder = new ServoMotorControl();
        imageRecognition = new RecognizeByFace();// todo remove new
        imageRecognition.addObserver(cameraHolder);

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
        COMPortChooseBox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (COMPortChooseBox.getSelectionModel().getSelectedItem() == null) {
                establishConnectionButton.setDisable(true);
            } else {
                establishConnectionButton.setDisable(false);
            }
            handleCloseSerialPortConnection();
        }));
        establishConnectionButton.setOnAction(event ->
                handleEstablishSerialPortConnection((String) COMPortChooseBox.getSelectionModel().getSelectedItem())
        );
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
        imageViewDimension(viewCamera, 600);// TODO resize after changing window's size
        imageViewDimension(viewMaskImage, 400);
        imageViewDimension(viewMorphImage, 400);

        try {
            imageRecognition.openVideoCapture(new HashMap<String, Object>() {{
                put("hueRangeStartSlider", hueRangeStartSlider);
                put("saturationRangeStartSlider", saturationRangeStartSlider);
                put("brightnessRangeStartSlider", brightnessRangeStartSlider);
                put("hueRangeEndSlider", hueRangeEndSlider);
                put("saturationRangeEndSlider", saturationRangeEndSlider);
                put("brightnessRangeEndSlider", brightnessRangeEndSlider);
                put("viewCamera", viewCamera);
                put("viewMaskImage", viewMaskImage);
                put("viewMorphImage", viewMorphImage);
            }});
        } catch (Exception e) {
            e.printStackTrace();
            logMessage(e.getMessage());
        }
        /* Initialize views and start video capture */

        handleChangeStartRangeColor();
        handleChangeEndRangeColor();
    }

    private void logMessage(String message) {
        Platform.runLater(() -> logTextArea.appendText(
                new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()) + " - " + message + "\n"));
    }

    private void handleEstablishSerialPortConnection(String portName) {
        try {
            cameraHolder.setUpConnection(new HashMap<String, Object>() {{
                put("portName", portName);
            }});
            cameraHolder.setHorizontalAngle(cameraHolder.getHorizontalAngleMaxValue() / 2);
            if (cameraHolder.isConnected()) {
                logMessage("Connection with COM port \"" + portName + "\" established");
            }
            closeConnectionButton.setDisable(!cameraHolder.isConnected());
            servoAngleSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                    cameraHolder.setHorizontalAngle((int) servoAngleSlider.getValue()));
            servoAngleSlider.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
            logMessage(e.getMessage());
            servoAngleSlider.setDisable(true);
        }
    }

    @FXML
    private void handleCloseSerialPortConnection() {
        boolean isConnectedValue = cameraHolder.isConnected();
        cameraHolder.closeConnection();
        closeConnectionButton.setDisable(!cameraHolder.isConnected());
        if (isConnectedValue != cameraHolder.isConnected()) {
            logMessage("Connection with COM port was closed");
        }
        servoAngleSlider.setDisable(true);
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
        cameraHolder.closeConnection();
        imageRecognition.closeVideoCapture();
    }

    /* Static methods */
    public static void imageViewDimension(ImageView imageView, int width) {
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(width);
    }
    /* Static methods */
}
