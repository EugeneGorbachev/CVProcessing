package ru.vsu.cvprocessing.controller;

import com.fazecast.jSerialComm.SerialPort;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
    private Button saveWebCameraIndexSaveButton;

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

    private IntegerProperty refreshCoordinatesFreq = new SimpleIntegerProperty();
    private DoubleProperty horizontalAngle = new SimpleDoubleProperty();
    private DoubleProperty verticalAngle = new SimpleDoubleProperty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveWebCameraIndexSaveButton.setDisable(true);
        establishConnectionButton.setDisable(true);
        closeConnectionButton.setDisable(true);
        horizontalAngleTextField.disableProperty().bind(closeConnectionButton.disabledProperty());
        horizontalAngleSlider.disableProperty().bind(closeConnectionButton.disabledProperty());
        verticalAngleTextField.disableProperty().bind(closeConnectionButton.disabledProperty());
        verticalAngleSlider.disableProperty().bind(closeConnectionButton.disabledProperty());

        webCameraIndexTextField.setText(String.valueOf(getInstance().getCamera().getWebCamIndex()));
        webCameraIndexTextField.textProperty().addListener(((observable, oldValue, newValue) -> {// TODO replace with some kind of boolean property
            try {
                Integer.parseInt(newValue);
                saveWebCameraIndexSaveButton.setDisable(false);
            } catch (NumberFormatException e) {
                saveWebCameraIndexSaveButton.setDisable(true);
            }
        }));

        refreshCoordinatesFreq.bindBidirectional(refreshCoordinatesFreqSlider.valueProperty());
        try {
            Bindings.bindBidirectional(refreshCoordinatesFreqTextField.textProperty(), refreshCoordinatesFreq,
                    new ValidationStringConverter((val) ->
                            val.doubleValue() >= refreshCoordinatesFreqSlider.getMin() &&
                                    val.doubleValue() <= refreshCoordinatesFreqSlider.getMax(),
                            "Refresh previous coordinates frequency", true
                    ));
        } catch (Exception e) {
            log.error(e);
        }
        refreshCoordinatesFreq.setValue(12);
        refreshCoordinatesFreq.addListener(((observable, oldValue, newValue) ->
                getInstance().getImageRecognition().setRefreshCoordinateFrequency(newValue.intValue())));

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

        horizontalAngleSlider.setMin(getInstance().getCameraHolder().getHorizontalAngleMinValue());
        horizontalAngleSlider.setMax(getInstance().getCameraHolder().getHorizontalAngleMaxValue());
        horizontalAngleSlider.setValue((int) (horizontalAngleSlider.getMin() +
                (horizontalAngleSlider.getMax() - horizontalAngleSlider.getMin()) / 2));
        try {
            horizontalAngle.bindBidirectional(horizontalAngleSlider.valueProperty());
            Bindings.bindBidirectional(horizontalAngleTextField.textProperty(), horizontalAngle,
                    new ValidationStringConverter(val ->
                            val.doubleValue() >= horizontalAngleSlider.getMin() &&
                                    val.doubleValue() <= horizontalAngleSlider.getMax(),
                            "Horizontal angle", false
                    ));
        } catch (Exception e) {
            log.error(e);
        }
        horizontalAngleSlider.valueProperty().addListener((observable, oldValue, newValue) -> sendingDataPublisher.publish(
                new SendingDataEvent(true, false, newValue.intValue())));

        verticalAngleSlider.setMin(getInstance().getCameraHolder().getVerticalAngleMinValue());
        verticalAngleSlider.setMax(getInstance().getCameraHolder().getVerticalAngleMaxValue());
        verticalAngleSlider.setValue((int) (verticalAngleSlider.getMin() +
                (verticalAngleSlider.getMax() - verticalAngleSlider.getMin()) / 2));
        try {
            verticalAngle.bindBidirectional(verticalAngleSlider.valueProperty());
            Bindings.bindBidirectional(verticalAngleTextField.textProperty(), verticalAngle,
                    new ValidationStringConverter(val ->
                            val.doubleValue() >= verticalAngleSlider.getMin() &&
                                    val.doubleValue() <= verticalAngleSlider.getMax(),
                            "Horizontal angle", false
                    ));
        } catch (Exception e) {
            log.error(e);
        }
        verticalAngleSlider.valueProperty().addListener(((observable, oldValue, newValue) -> sendingDataPublisher.publish(
                new SendingDataEvent(true, true, newValue.intValue())
        )));
    }

    /* Auxiliary  */
    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
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

    }

    @FXML
    private void handleSaveVerticalAngleMax() {

    }

    @FXML
    private void handleSaveHorizontalAngleMin() {

    }

    @FXML
    private void handleSaveHorizontalAngleMax() {

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
