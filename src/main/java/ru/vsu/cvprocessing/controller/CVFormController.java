package ru.vsu.cvprocessing.controller;

import ru.vsu.cvprocessing.holder.Camera;
import ru.vsu.cvprocessing.holder.CameraHolder;
import com.fazecast.jSerialComm.SerialPort;
import ru.vsu.cvprocessing.recognition.FakeImageRecognition;
import ru.vsu.cvprocessing.recognition.ImageRecognition;
import ru.vsu.cvprocessing.recognition.RecognizeByCascade;
import ru.vsu.cvprocessing.holder.ServoMotorControl;
import ru.vsu.cvprocessing.recognition.RecognizeByColor;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CVFormController implements Initializable {
    private final static double MIN_IMAGE_VIEW_HEIGH = 266.6d;
    private final static double MIN_IMAGE_VIEW_WIDTH = 400d;

    enum ImageRecognithionMethods {
        NONE, RECOGNIZE_BY_COLOR, RECOGNIZE_BY_CASCADE
    }

    @FXML
    private SplitPane mainSplitPane;
    @FXML
    private AnchorPane leftSideOfSplitPane;
    @FXML
    private AnchorPane rightSideOfSplitPane;

    /* Central part*/
    @FXML
    private ImageView viewCamera;
    /*Central part*/

    /* Program settings */
    @FXML
    private ChoiceBox IRMethodChooseBox;
    @FXML
    private ColorPicker markerColorPicker;
    @FXML
    private ChoiceBox COMPortChooseBox;
    @FXML
    private Button establishConnectionButton;
    @FXML
    private Button closeConnectionButton;
    @FXML
    private Slider servoHorizontalAngleSlider;
    @FXML
    private TextArea logTextArea;
    /* Program settings */

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

    /* Recognize by Haar cascade */
    @FXML
    private ChoiceBox haarCascadeChooseBox;
    @FXML
    private TextArea previewHaarCascadeTextArea;
    /* Recognize by Haar cascade */

    private List<Camera> cameraList;
    private CameraHolder cameraHolder;
    private ImageRecognition imageRecognition;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cameraList = new ArrayList<Camera>() {{
            add(new Camera(0, 70, 400d, 600d));
        }};
        // TODO add selector for camera

        cameraHolder = new ServoMotorControl(cameraList.get(0));// TODO remove in case of new realization CameraHolder
        imageRecognition = new FakeImageRecognition(cameraList.get(0));// TODO replace hardcode
//        handleChangeDividerPosition();
        handleSwitchToNone();

        mainSplitPane.getDividers().get(0).positionProperty().addListener(observable -> handleChangeDividerPosition());

        /* Initialize settings */
        IRMethodChooseBox.setItems(FXCollections.observableArrayList(ImageRecognithionMethods.values()));
        IRMethodChooseBox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            switch ((ImageRecognithionMethods) IRMethodChooseBox.getSelectionModel().getSelectedItem()) {
                case RECOGNIZE_BY_COLOR:
                    handleSwitchToRecognizeByColor();
                    break;
                case RECOGNIZE_BY_CASCADE:
                    handleSwitchToRecognizeByCascade();
                    break;
                default:
                    handleSwitchToNone();
            }
        }));

        markerColorPicker.setValue(imageRecognition.getMarkerColor());
        markerColorPicker.valueProperty().addListener(((observable, oldValue, newValue) -> {
            imageRecognition.setMarkerColor(newValue);
        }));

        // Servo settings
        COMPortChooseBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(SerialPort.getCommPorts())
                        .map(serialPort -> serialPort.getSystemPortName())
                        .filter(s -> s.startsWith("tty") || s.startsWith("COM"))
                        .collect(Collectors.toList())
                )
        );
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
        /* Initialize settings */

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

        /* Initialize recognize by color */
        List<String> haarCascades = new ArrayList<>();
        File haarCascadesDirectory = new File(getClass().getClassLoader().getResource("haarcascades").getPath());
        for (final File file : haarCascadesDirectory.listFiles()) {
            haarCascades.add(file.getName());
        }
        haarCascadeChooseBox.setItems(FXCollections.observableArrayList(haarCascades));

        haarCascadeChooseBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                handleSwitchToRecognizeByCascade();

                /* Show preview in TextArea */
                BufferedReader bufferedReader = new BufferedReader(
                        new FileReader(haarCascadesDirectory.getAbsolutePath() + "/" + newValue)
                );

                String line;
                previewHaarCascadeTextArea.clear();
                for (int i = 0; (line = bufferedReader.readLine()) != null && i < 100; i++) {
                    previewHaarCascadeTextArea.appendText(line + "\n");
                }
                if (line != null) {
                    previewHaarCascadeTextArea.appendText("...");
                }
                previewHaarCascadeTextArea.positionCaret(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        /* Initialize recognize by color */

        handleChangeStartRangeColor();
        handleChangeEndRangeColor();
    }

    private void logMessage(String message) {
        Platform.runLater(() -> logTextArea.appendText(
                new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()) + " - " + message + "\n"));
    }

    /* Handles for switch recognition type */
    private void handleSwitchToNone() {
        imageRecognition.closeVideoCapture();
        imageRecognition = new FakeImageRecognition(cameraList.get(0));// TODO replace hardcode

        try {
            imageRecognition.openVideoCapture(new HashMap<String, Object>() {{
                put("viewCamera", viewCamera);
            }});
            IRMethodChooseBox.getSelectionModel().select(ImageRecognithionMethods.NONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSwitchToRecognizeByColor() {
        imageRecognition.closeVideoCapture();
        imageRecognition = new RecognizeByColor(cameraList.get(0));// TODO replace hardcode
        imageRecognition.addObserver(cameraHolder);

        setImageViewWidth(viewMaskImage, rightSideOfSplitPane.getWidth() / 2);
        setImageViewWidth(viewMorphImage, rightSideOfSplitPane.getWidth() / 2);

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
            IRMethodChooseBox.getSelectionModel().select(ImageRecognithionMethods.RECOGNIZE_BY_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
            logMessage(e.getMessage());
        }
    }

    private void handleSwitchToRecognizeByCascade() {
        imageRecognition.closeVideoCapture();
        String cascadeConfigName = (String) haarCascadeChooseBox.getSelectionModel().getSelectedItem();
        if (cascadeConfigName == null) {
            cascadeConfigName = (String) haarCascadeChooseBox.getItems().get(0);
            haarCascadeChooseBox.getSelectionModel().select(0);
        }
        imageRecognition = new RecognizeByCascade(cameraList.get(0),// TODO replace hardcode
                getClass().getClassLoader().getResource("haarcascades/" + cascadeConfigName).getPath());
        imageRecognition.addObserver(cameraHolder);

        try {
            imageRecognition.openVideoCapture(new HashMap<String, Object>() {{
                put("viewCamera", viewCamera);
            }});
            IRMethodChooseBox.getSelectionModel().select(ImageRecognithionMethods.RECOGNIZE_BY_CASCADE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /* Handles for switch recognition type */

    /* Handles for open/close serial port connection */
    private void handleEstablishSerialPortConnection(String portName) {
        try {
            cameraHolder.setUpConnection(new HashMap<String, Object>() {{
                put("portName", portName);
            }});
            cameraHolder.setHorizontalAngle(cameraHolder.getHorizontalAngleMaxValue() / 2);
            ((ServoMotorControl) cameraHolder).sendSingleByte(ServoMotorControl.mapIntToByteValue((int) servoHorizontalAngleSlider.getValue()));
            if (cameraHolder.isConnected()) {
                logMessage("Connection with COM port \"" + portName + "\" established");
            }
            closeConnectionButton.setDisable(!cameraHolder.isConnected());
            servoHorizontalAngleSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    cameraHolder.setHorizontalAngle((int) servoHorizontalAngleSlider.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ((ServoMotorControl) cameraHolder).sendSingleByte(ServoMotorControl.mapIntToByteValue((int) servoHorizontalAngleSlider.getValue()));
            });
            servoHorizontalAngleSlider.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
            logMessage(e.getMessage());
            servoHorizontalAngleSlider.setDisable(true);
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
        servoHorizontalAngleSlider.setDisable(true);
    }
    /* Handles for open/close serial port connection */

    /* Handles for changing parameters */
    private void handleChangeDividerPosition() {
        double height = leftSideOfSplitPane.getHeight() > 0 ? leftSideOfSplitPane.getHeight() : 400d;
        double width = leftSideOfSplitPane.getWidth() > 0 ? leftSideOfSplitPane.getWidth() : 600d;
        setImageViewDimension(viewCamera, true,
                height <= MIN_IMAGE_VIEW_HEIGH ? MIN_IMAGE_VIEW_HEIGH : height,
                width <= MIN_IMAGE_VIEW_WIDTH ? MIN_IMAGE_VIEW_WIDTH : width
        );
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
    /* Handles for changing parameters */

    public void handleClose() {
        cameraHolder.closeConnection();
        imageRecognition.closeVideoCapture();
    }

    /* Static methods */
    private void setImageViewDimension(ImageView imageView, boolean preserveRatio, double height, double width) {
        imageView.setPreserveRatio(preserveRatio);
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
    }

    private void setImageViewHeight(ImageView imageView, double height) {
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(height);
    }

    private void setImageViewWidth(ImageView imageView, double width) {
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(width);
    }
    /* Static methods */
}
