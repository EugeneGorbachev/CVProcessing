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
import ru.vsu.cvprocessing.event.ChangeIRMethodEvent;
import ru.vsu.cvprocessing.event.IRMethodPublisher;
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
    private ChoiceBox irMethodChoiceBox;
    @FXML
    private ColorPicker markerColorPicker;

    @FXML
    private ChoiceBox comPortChoiceBox;
    @FXML
    private Button establishConnectionButton;
    @FXML
    private Button closeConnectionButton;
    @FXML
    private Slider horizontalAngleSlider;

    @Autowired
    private IRMethodPublisher irMethodPublisher;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webcameraIndexTextField.setText(String.valueOf(getInstance().getCamera().getWebcamIndex()));
        webcameraIndexTextField.textProperty().addListener(((observable, oldValue, newValue) -> {//TODO replace with combobox
            if (newValue.matches("\\d+")) {
                handleChangedWebcameraIndex(Integer.valueOf(newValue));
            } else {
                log.error("Camera index must be a number");
                webcameraIndexTextField.setText(oldValue);
            }
        }));

        irMethodChoiceBox.setItems(FXCollections.observableArrayList(ImageRecognitionMethod.values()));
        irMethodChoiceBox.setValue(getInstance().getImageRecognition().getImageRecognitionMethod());
        irMethodChoiceBox.valueProperty().addListener(((observable, oldValue, newValue) ->
                irMethodPublisher.publish(new ChangeIRMethodEvent(this,
                        (ImageRecognitionMethod) oldValue, (ImageRecognitionMethod) newValue))));

        markerColorPicker.setValue(getInstance().getMarkerColor());

        comPortChoiceBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(SerialPort.getCommPorts())
                        .map(SerialPort::getSystemPortName)
                        .filter(s -> s.startsWith("tty") || s.startsWith("COM"))
                        .collect(Collectors.toList())
                )
        );
        comPortChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> handleChangedCOMPort());
    }

    private void handleChangedWebcameraIndex(int cameraIndex) {
        getInstance().getCamera().setWebcamIndex(cameraIndex);
        log.info("Camera cameraIndex value was changed on " + cameraIndex);
    }

    @FXML
    private void handleChangedMarkerColor() {
        getInstance().setMarkerColor(markerColorPicker.getValue());
        log.info("Marker color was changed to " + markerColorPicker.getValue());
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
    private void handleEstablishConnection() {
        establishConnectionButton.setDisable(true);
        closeConnectionButton.setDisable(false);
        horizontalAngleSlider.setDisable(false);
    }

    @FXML
    private void handleCloseConnection() {
        establishConnectionButton.setDisable(false);
        closeConnectionButton.setDisable(true);
        horizontalAngleSlider.setDisable(true);

        if (getInstance().getCameraHolder().closeConnection()) {
            log.info("Connection with COM port was closed");
        }
    }

    @EventListener
    public void handleChangeIRMethod(ChangeIRMethodEvent event) {
        if (irMethodChoiceBox != null) {
            irMethodChoiceBox.setValue(event.getNewValue());
        }
    }
}
