package ru.vsu.cvprocessing.controller;

import com.fazecast.jSerialComm.SerialPort;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.vsu.cvprocessing.event.*;
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

    @FXML
    private TextField webcameraIndexTextField;
    @FXML
    private Button saveWebcameraIndexButton;
    @FXML
    private ChoiceBox irMethodChoiceBox;
    @FXML
    private ColorPicker markerColorPicker;

    @FXML
    private ChoiceBox comPortChoiceBox;
    @FXML
    private Slider horizontalAngleSlider;
    @FXML
    private Slider verticalAngleSlider;
    @FXML
    private Button establishConnectionButton;
    @FXML
    private Button closeConnectionButton;

    @Autowired
    private IRMethodPublisher irMethodPublisher;
    @Autowired
    private SendingDataPublisher sendingDataPublisher;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveWebcameraIndexButton.setDisable(true);

        webcameraIndexTextField.setText(String.valueOf(getInstance().getCamera().getWebcamIndex()));
        webcameraIndexTextField.textProperty().addListener(((observable, oldValue, newValue) -> {//TODO replace with combobox
            try {
                Integer.parseInt(newValue);
                saveWebcameraIndexButton.setDisable(false);
            } catch (NumberFormatException e) {
                saveWebcameraIndexButton.setDisable(true);
            }
        }));

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

        horizontalAngleSlider.valueProperty().addListener((observable, oldValue, newValue) -> sendingDataPublisher.publish(
                new SendingDataEvent(true, false, (int) horizontalAngleSlider.getValue())));
        verticalAngleSlider.valueProperty().addListener(((observable, oldValue, newValue) -> sendingDataPublisher.publish(
                new SendingDataEvent(true, true, (int) verticalAngleSlider.getValue())
        )));
    }

    private void handleChangedCOMPort() {
        if (comPortChoiceBox.getSelectionModel().getSelectedItem() != null) {
            establishConnectionButton.setDisable(false);
        } else {
            establishConnectionButton.setDisable(true);
        }
        handleCloseConnection();
    }

    @FXML
    private void handleSaveWebcameraIndex() {
        int webcamIndex = Integer.parseInt(webcameraIndexTextField.getText());
        getInstance().getCamera().setWebcamIndex(webcamIndex);
        irMethodPublisher.publish(new IRMethodChangedEvent(this,
                (ImageRecognitionMethod) irMethodChoiceBox.getValue(),
                (ImageRecognitionMethod) irMethodChoiceBox.getValue()
        ));
        log.info("Camera cameraIndex value was changed on " + webcamIndex);
    }

    @FXML
    private void handleEstablishConnection() {
        establishConnectionButton.setDisable(true);
        closeConnectionButton.setDisable(false);
        horizontalAngleSlider.setDisable(false);
        verticalAngleSlider.setDisable(false);

        try {
            getInstance().getCameraHolder().setUpConnection(new HashMap<String, Object>() {{
                put("portName", comPortChoiceBox.getValue());
            }});
            getInstance().getCameraHolder().setHorizontalAngle(getInstance().getCameraHolder().getHorizontalAngleMaxValue() / 2);
        } catch (Exception e) {
            log.error(e);
            horizontalAngleSlider.setDisable(true);
            verticalAngleSlider.setDisable(true);
        }
    }

    @FXML
    private void handleCloseConnection() {
        establishConnectionButton.setDisable(false);
        closeConnectionButton.setDisable(true);
        horizontalAngleSlider.setDisable(true);
        verticalAngleSlider.setDisable(true);

        if (getInstance().getCameraHolder().closeConnection()) {
            log.info("Connection with COM port was closed");
        }
    }


    @EventListener
    public void handleSendingData(SendingDataEvent event) {
        ((ServoMotorControl) getInstance().getCameraHolder()).sendInt(event.getPreferences());
        log.info(String.format("Sent preferences to Arduino as %s", Integer.toBinaryString(event.getPreferences())));

        ((ServoMotorControl) getInstance().getCameraHolder()).sendInt(event.getValue());
        log.info(String.format("Sent %d to Arduino as %s", event.getValue(), Integer.toBinaryString(event.getValue())));
    }


    @EventListener
    public void handleChangeIRMethod(IRMethodChangedEvent event) {
        if (irMethodChoiceBox != null) {
            irMethodChoiceBox.setValue(event.getNewValue());
        }
    }
}
