package ru.vsu.cvprocessing.controller;

import com.fazecast.jSerialComm.SerialPort;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.vsu.cvprocessing.event.IRMethodChangedEvent;
import ru.vsu.cvprocessing.event.IRMethodPublisher;
import ru.vsu.cvprocessing.event.SendingDataEvent;
import ru.vsu.cvprocessing.event.SendingDataPublisher;
import ru.vsu.cvprocessing.holder.ServoMotorControl;
import ru.vsu.cvprocessing.recognition.ImageRecognitionMethod;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

@Component
public class SettingsController implements Initializable {
    private static final Logger log = Logger.getLogger(SettingsController.class);

    /* Test connection tab */
    @FXML
    private ChoiceBox comPortChoiceBox;

    @FXML
    private TextField verticalAngleTextField;
    @FXML
    private Slider verticalAngleSlider;

    @FXML
    private TextField horizontalAngleTextField;
    @FXML
    private Slider horizontalAngleSlider;

    @FXML
    private Button establishConnectionButton;
    @FXML
    private Button closeConnectionButton;

    /* Max and min value tab */
    @FXML
    private TextField verticalAngleMinTextField;
    @FXML
    private Button verticalAngleMinSaveButton;

    @FXML
    private TextField verticalAngleMaxTextField;
    @FXML
    private Button verticalAngleMaxSaveButton;

    @FXML
    private TextField horizontalAngleMinTextField;
    @FXML
    private Button horizontalAngleMinSaveButton;

    @FXML
    private TextField horizontalAngleMaxTextField;
    @FXML
    private Button horizontalAngleMaxSaveButton;

    /* Camera settings tab */
    @FXML
    private TextField webCameraIndexTextField;
    @FXML
    private Button webCameraIndexSaveButton;

    /* Image recognition settings tab */
    @FXML
    private ChoiceBox irMethodChoiceBox;
    @FXML
    private ColorPicker markerColorPicker;

    @FXML
    private TextField refreshCoordinatesFreqTextField;
    @FXML
    private Slider refreshCoordinatesFreqSlider;

    @Autowired
    private IRMethodPublisher irMethodPublisher;
    @Autowired
    private SendingDataPublisher sendingDataPublisher;

    private IntegerProperty refreshCoordinatesFreq = new SimpleIntegerProperty(12);
    private DoubleProperty horizontalAngleValue = new SimpleDoubleProperty();
    private DoubleProperty verticalAngleValue = new SimpleDoubleProperty();
    private IntegerProperty verticalAngleMinValue = new SimpleIntegerProperty(getInstance().getCameraHolder().getVerticalAngleMinValue());
    private IntegerProperty verticalAngleMaxValue = new SimpleIntegerProperty(getInstance().getCameraHolder().getVerticalAngleMaxValue());
    private IntegerProperty horizontalAngleMinValue = new SimpleIntegerProperty(getInstance().getCameraHolder().getHorizontalAngleMinValue());
    private IntegerProperty horizontalAngleMaxValue = new SimpleIntegerProperty(getInstance().getCameraHolder().getHorizontalAngleMaxValue());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        establishConnectionButton.setDisable(true);
        closeConnectionButton.setDisable(true);

        horizontalAngleTextField.disableProperty().bind(closeConnectionButton.disabledProperty());
        horizontalAngleSlider.disableProperty().bind(closeConnectionButton.disabledProperty());
        verticalAngleTextField.disableProperty().bind(closeConnectionButton.disabledProperty());
        verticalAngleSlider.disableProperty().bind(closeConnectionButton.disabledProperty());

        webCameraIndexTextField.setText(String.valueOf(getInstance().getCamera().getWebCamIndex()));
        webCameraIndexSaveButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                !tryParseInt(webCameraIndexTextField.textProperty().get()), webCameraIndexTextField.textProperty()));

        verticalAngleMinSaveButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            String value = verticalAngleMinTextField.textProperty().get();
            if (tryParseInt(value)) {
                int parsedInt = Integer.parseInt(value);
                return !(validateAngleValue(parsedInt) && parsedInt < verticalAngleMaxValue.get());
            }
            return true;
        }, verticalAngleMinTextField.textProperty()));
        verticalAngleMaxSaveButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            String value = verticalAngleMaxTextField.textProperty().get();
            if (tryParseInt(value)) {
                int parsedInt = Integer.parseInt(value);
                return !(validateAngleValue(parsedInt) && parsedInt > verticalAngleMinValue.get());
            }
            return true;
        }, verticalAngleMaxTextField.textProperty()));
        horizontalAngleMinSaveButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            String value = horizontalAngleMinTextField.textProperty().get();
            if (!tryParseInt(value)) {
                return true;
            }
            int parsedInt = Integer.parseInt(value);
            return !(validateAngleValue(parsedInt) && parsedInt < horizontalAngleMaxValue.get());
        }, horizontalAngleMinTextField.textProperty()));
        horizontalAngleMaxSaveButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            String value = horizontalAngleMaxTextField.textProperty().get();
            if (!tryParseInt(value)) {
                return true;
            }
            int parsedInt = Integer.parseInt(value);
            return !(validateAngleValue(parsedInt) && parsedInt > horizontalAngleMinValue.get());
        }, horizontalAngleMaxTextField.textProperty()));

        /* Binding Slider with Textfield */
        ValidationStringConverter verticalAngleConverter = new ValidationStringConverter((val) ->
                val.doubleValue() >= verticalAngleSlider.getMin() && val.doubleValue() <= verticalAngleSlider.getMax(),
                0, "Vertical angle", true);
        ValidationStringConverter horizontalAngleConverter = new ValidationStringConverter((val) ->
                val.doubleValue() >= horizontalAngleSlider.getMin() && val.doubleValue() <= horizontalAngleSlider.getMax(),
                0, "Horizontal angle", true);
        ValidationStringConverter refreshCoordinatesFreqConverter = new ValidationStringConverter((val) ->
                val.doubleValue() >= refreshCoordinatesFreqSlider.getMin() && val.doubleValue() <= refreshCoordinatesFreqSlider.getMax(),
                1, "Refresh previous coordinates frequency", true);
        ValidationStringConverter angleMinMaxConverter =
                new ValidationStringConverter((val) -> true, 0, "Vertical or horizontal angle", false);

        //TODO may be needs try catch
        verticalAngleValue.bindBidirectional(verticalAngleSlider.valueProperty());
        Bindings.bindBidirectional(verticalAngleTextField.textProperty(), verticalAngleValue, verticalAngleConverter);
        horizontalAngleValue.bindBidirectional(horizontalAngleSlider.valueProperty());
        Bindings.bindBidirectional(horizontalAngleTextField.textProperty(), horizontalAngleValue, horizontalAngleConverter);
        refreshCoordinatesFreq.bindBidirectional(refreshCoordinatesFreqSlider.valueProperty());
        Bindings.bindBidirectional(refreshCoordinatesFreqTextField.textProperty(), refreshCoordinatesFreq, refreshCoordinatesFreqConverter);

        Bindings.bindBidirectional(verticalAngleMinTextField.textProperty(), verticalAngleMinValue, angleMinMaxConverter);
        Bindings.bindBidirectional(verticalAngleMaxTextField.textProperty(), verticalAngleMaxValue, angleMinMaxConverter);
        Bindings.bindBidirectional(horizontalAngleMinTextField.textProperty(), horizontalAngleMinValue, angleMinMaxConverter);
        Bindings.bindBidirectional(horizontalAngleMaxTextField.textProperty(), horizontalAngleMaxValue, angleMinMaxConverter);

        refreshCoordinatesFreq.addListener(((observable, oldValue, newValue) ->
                getInstance().getImageRecognition().setRefreshCoordinateFrequency(newValue.intValue())));
        horizontalAngleValue.addListener(((observable, oldValue, newValue) ->
                sendingDataPublisher.publish(new SendingDataEvent(true, false, newValue.intValue()))));
        verticalAngleValue.addListener(((observable, oldValue, newValue) ->
                sendingDataPublisher.publish(new SendingDataEvent(true, true, newValue.intValue()))));
        /* Binding Slider with Textfield */

        irMethodChoiceBox.setItems(FXCollections.observableArrayList(ImageRecognitionMethod.values()));
        irMethodChoiceBox.setValue(getInstance().getImageRecognition().getImageRecognitionMethod());
        irMethodChoiceBox.valueProperty().addListener(((observable, oldValue, newValue) ->
                irMethodPublisher.publish(new IRMethodChangedEvent(this,
                        (ImageRecognitionMethod) oldValue, (ImageRecognitionMethod) newValue))));

        markerColorPicker.valueProperty().bindBidirectional(getInstance().markerColorPropertyProperty());

        comPortChoiceBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(SerialPort.getCommPorts())
                        .map(SerialPort::getSystemPortName)
                        .filter(s -> s.startsWith("tty") || s.startsWith("COM"))
                        .collect(Collectors.toList())
                )
        );
        comPortChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> handleChangedCOMPort());

        horizontalAngleSlider.setValue((int) (horizontalAngleSlider.getMin() +
                (horizontalAngleSlider.getMax() - horizontalAngleSlider.getMin()) / 2));
        verticalAngleSlider.setValue((int) (verticalAngleSlider.getMin() +
                (verticalAngleSlider.getMax() - verticalAngleSlider.getMin()) / 2));
    }

    /* Auxiliary  */
    private boolean validateAngleValue(int val) {
        return val >= 0 && val <= 360;
    }

    private boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /* Form operations handlers */
    @FXML
    private void handleSaveWebCameraIndex() {
        int webcamIndex = Integer.parseInt(webCameraIndexTextField.getText());
        getInstance().getCamera().setWebCamIndex(webcamIndex);
        irMethodPublisher.publish(new IRMethodChangedEvent(this,
                (ImageRecognitionMethod) irMethodChoiceBox.getValue(),
                (ImageRecognitionMethod) irMethodChoiceBox.getValue()
        ));
        log.info("Camera cameraIndex value was changed on " + webcamIndex);
    }

    @FXML
    private void handleSaveVerticalAngleMin() {
        verticalAngleSlider.setMin(verticalAngleMinValue.doubleValue());
        getInstance().setCameraHolder(new ServoMotorControl.ServoMotorControlBuilder()
                .fromObject(getInstance().getCameraHolder())
                .setVerticalAngleMinValue(verticalAngleMinValue.get())
                .build()
        );
        log.info("Vertical angle's min was changed on " + verticalAngleMinValue.get());
    }

    @FXML
    private void handleSaveVerticalAngleMax() {
        verticalAngleSlider.setMax(verticalAngleMaxValue.doubleValue());
        getInstance().setCameraHolder(new ServoMotorControl.ServoMotorControlBuilder()
                .fromObject(getInstance().getCameraHolder())
                .setVerticalAngleMaxValue(verticalAngleMaxValue.get())
                .build()
        );
        log.info("Vertical angle's max was changed on " + verticalAngleMaxValue.get());
    }

    @FXML
    private void handleSaveHorizontalAngleMin() {
        horizontalAngleSlider.setMin(horizontalAngleMinValue.doubleValue());
        getInstance().setCameraHolder(new ServoMotorControl.ServoMotorControlBuilder()
                .fromObject(getInstance().getCameraHolder())
                .setHorizontalAngleMinValue(horizontalAngleMinValue.get())
                .build()
        );
        log.info("Horizontal angle's min was changed on " + horizontalAngleMinValue.get());
    }

    @FXML
    private void handleSaveHorizontalAngleMax() {
        horizontalAngleSlider.setMax(horizontalAngleMaxValue.doubleValue());
        getInstance().setCameraHolder(new ServoMotorControl.ServoMotorControlBuilder()
                .fromObject(getInstance().getCameraHolder())
                .setHorizontalAngleMaxValue(horizontalAngleMaxValue.get())
                .build()
        );
        log.info("Horizontal angle's max was changed on " + horizontalAngleMaxValue.get());
    }

    @FXML
    private void handleEstablishConnection() {
        establishConnectionButton.setDisable(true);
        closeConnectionButton.setDisable(false);

        try {
            getInstance().getCameraHolder().setUpConnection(new HashMap<String, Object>() {{
                put("portName", comPortChoiceBox.getValue());
            }});
        } catch (Exception e) {
            log.error(e);
            closeConnectionButton.setDisable(true);
        }
    }

    @FXML
    private void handleCloseConnection() {
        establishConnectionButton.setDisable(false);
        closeConnectionButton.setDisable(true);

        if (getInstance().getCameraHolder().closeConnection()) {
            log.info("Connection with COM port was closed");
        }
    }

    private void handleChangedCOMPort() {
        if (comPortChoiceBox.getSelectionModel().getSelectedItem() != null) {
            establishConnectionButton.setDisable(false);
        } else {
            establishConnectionButton.setDisable(true);
        }
        handleCloseConnection();
    }


    /* Event handling */
    @EventListener
    public void handleSendingData(SendingDataEvent event) {
        ServoMotorControl servoMotorControl = (ServoMotorControl) getInstance().getCameraHolder();
        if (getInstance().getSendDetectionData()) {
            if (servoMotorControl.isConnected()) {
                servoMotorControl.moveServo(event.isDetected(), event.isVertical(), event.getValue());
            } else {
                log.error("Cannot send data. COM port connection not established.");
            }
        }
    }


    @EventListener
    public void handleChangeIRMethod(IRMethodChangedEvent event) {
        if (irMethodChoiceBox != null) {
            irMethodChoiceBox.setValue(event.getNewValue());
        }
    }
}
