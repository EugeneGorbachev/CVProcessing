package ru.vsu.cvprocessing.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.vsu.cvprocessing.settings.SettingsHolder;

import java.io.IOException;
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

    @FXML
    private ScrollPane rangeColorStartPane;
    @FXML
    private ScrollPane rangeColorEndPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colorRangeStartPicker.valueProperty().bindBidirectional(getInstance().colorRangeStartPropertyProperty());
        colorRangeEndPicker.valueProperty().bindBidirectional(getInstance().colorRangeEndPropertyProperty());

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(SettingsHolder.FXML_FILE_PREF + "hsbcolorpicker.fxml"));
            rangeColorStartPane.setContent(fxmlLoader.load());
            ((HSBColorPickerController) fxmlLoader.getController()).bindColorProperty(getInstance().colorRangeStartPropertyProperty());

            fxmlLoader = new FXMLLoader(getClass().getResource(SettingsHolder.FXML_FILE_PREF + "hsbcolorpicker.fxml"));
            rangeColorEndPane.setContent(fxmlLoader.load());
            ((HSBColorPickerController) fxmlLoader.getController()).bindColorProperty(getInstance().colorRangeEndPropertyProperty());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e);
        }
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
