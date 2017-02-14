package ru.vsu.cvprocessing.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.vsu.cvprocessing.event.ColorChangedEvent;
import ru.vsu.cvprocessing.event.ColorChangedPublisher;
import ru.vsu.cvprocessing.event.ColorType;

import java.net.URL;
import java.util.ResourceBundle;

import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

@Component
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

    @Autowired
    private ColorChangedPublisher colorChangedPublisher;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colorRangeStartPicker.setValue(getInstance().getColorRangeStart());
        colorRangeEndPicker.setValue(getInstance().getColorRangeEnd());

        colorRangeStartPicker.valueProperty().addListener(
                ((observable, oldValue, newValue) -> handleChangeStartColorPickerValue(newValue)));
        colorRangeEndPicker.valueProperty().addListener(
                ((observable, oldValue, newValue) -> handleChangeEndColorPickerValue(newValue)));
    }

    private void handleChangeStartColorPickerValue(Color newValue) {
        colorChangedPublisher.publish(new ColorChangedEvent(this, ColorType.COLOR_RANGE_START,
                newValue.getHue(), newValue.getSaturation(), newValue.getBrightness()));
    }

    private void handleChangeEndColorPickerValue(Color newValue) {
        colorChangedPublisher.publish(new ColorChangedEvent(this, ColorType.COLOR_RANGE_END,
                newValue.getHue(), newValue.getSaturation(), newValue.getBrightness()));
    }

    @EventListener
    public void handleColorChangedEvent(ColorChangedEvent event) {
        Color oldColor = getInstance().getColorRangeStart();
        double newColorHue = event.getHue() == null ? oldColor.getHue() : event.getHue();
        double newColorSaturation = event.getSaturation() == null ? oldColor.getSaturation() : event.getSaturation();
        double newColorBrightness = event.getBrightness() == null ? oldColor.getBrightness() : event.getBrightness();
        String colorTypeNamePreffix = "";
        switch (event.getColorType()) {
            case COLOR_RANGE_START:
                colorTypeNamePreffix = "Start";
                getInstance().setColorRangeStart(Color.hsb(newColorHue, newColorSaturation, newColorBrightness));
                break;
            case COLOR_RANGE_END:
                colorTypeNamePreffix = "End";
                getInstance().setColorRangeEnd(Color.hsb(newColorHue, newColorSaturation, newColorBrightness));
                break;
            case MARKER:
                colorTypeNamePreffix = "Marker";
                getInstance().setMarkerColor(Color.hsb(newColorHue, newColorSaturation, newColorBrightness));
                break;
        }
        log.info(String.format("%s color range value was changed from ", colorTypeNamePreffix) +
                String.format("H:%1.2f S:%1.2f B:%1.2f", oldColor.getHue(), oldColor.getSaturation(), oldColor.getBrightness()) +
                String.format(" to H:%1.2f S:%1.2f B:%1.2f", newColorHue, newColorSaturation, newColorBrightness));
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
