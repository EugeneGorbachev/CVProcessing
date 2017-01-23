package ru.vsu.cvprocessing.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

public class IRByColorController implements Initializable {
    private static final Logger log = Logger.getLogger(IRByColorController.class);

    @FXML
    private ImageView maskImageView;
    @FXML
    private ImageView morphImageView;
    @FXML
    private ColorPicker colorRangeStartPicker;
    @FXML
    private ColorPicker colorRangeEndPicker;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colorRangeStartPicker.setValue(getInstance().getColorRangeStart());
        colorRangeEndPicker.setValue(getInstance().getColorRangeEnd());

        colorRangeStartPicker.valueProperty().addListener(
                ((observable, oldValue, newValue) -> handleChangeStartColorPickerValue(oldValue, newValue)));
        colorRangeEndPicker.valueProperty().addListener(
                ((observable, oldValue, newValue) -> handleChangeEndColorPickerValue(oldValue, newValue)));
    }

    private void handleChangeStartColorPickerValue(Color oldValue, Color newValue) {
        getInstance().setColorRangeStart(newValue);
        log.info("Start color range value was changed from " +
                String.format("H:%1.2f S:%1.2f B:%1.2f", oldValue.getHue(), oldValue.getSaturation(), oldValue.getBrightness()) +
                String.format(" to H:%1.2f S:%1.2f B:%1.2f", newValue.getHue(), newValue.getSaturation(), newValue.getBrightness()));
    }

    private void handleChangeEndColorPickerValue(Color oldValue, Color newValue) {
        getInstance().setColorRangeEnd(newValue);
        log.info("End color range value was changed from " +
                String.format("H:%1.2f S:%1.2f B:%1.2f", oldValue.getHue(), oldValue.getSaturation(), oldValue.getBrightness()) +
                String.format(" to H:%1.2f S:%1.2f B:%1.2f", newValue.getHue(), newValue.getSaturation(), newValue.getBrightness()));
    }

    /* Getters and setters */
    public ImageView getMaskImageView() {
        return maskImageView;
    }

    public ImageView getMorphImageView() {
        return morphImageView;
    }
    /* Getters and setters */
}
