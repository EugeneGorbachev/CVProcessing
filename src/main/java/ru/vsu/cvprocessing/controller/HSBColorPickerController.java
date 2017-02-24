package ru.vsu.cvprocessing.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class HSBColorPickerController implements Initializable {
    private static final Logger log = Logger.getLogger(HSBColorPickerController.class);

    @FXML
    private Pane currentColorPane;
    @FXML
    private Pane newColorPane;

    @FXML
    private Slider hueSlider;
    @FXML
    private Slider saturationSlider;
    @FXML
    private Slider brightnessSlider;

    @FXML
    private TextField hueTextField;
    @FXML
    private TextField saturationTextField;
    @FXML
    private TextField brightnessTextField;

    private Property<Color> colorProperty = new SimpleObjectProperty<>();
    private DoubleProperty hue = new SimpleDoubleProperty();
    private DoubleProperty saturation = new SimpleDoubleProperty();
    private DoubleProperty brightness = new SimpleDoubleProperty();

    public void bindColorProperty(ObjectProperty<Color> colorProperty) {
        this.colorProperty.bindBidirectional(colorProperty);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* Bind currentColorPane's background to colorProperty */
        colorProperty.addListener(((observable, oldValue, newValue) ->
                currentColorPane.setBackground(
                        new Background(new BackgroundFill(newValue, CornerRadii.EMPTY, Insets.EMPTY)))
        ));
        /* Bind currentColorPane's background to colorProperty */

        /* Bind double properties to Sliders and TextFields*/
        ValidationStringConverter hueConverter = new ValidationStringConverter(val ->
                val.doubleValue() >= hueSlider.getMin() && val.doubleValue() <= hueSlider.getMax(),
                "hue slider", false);
        ValidationStringConverter saturationConverter = new ValidationStringConverter(val ->
                val.doubleValue() >= saturationSlider.getMin() && val.doubleValue()<= saturationSlider.getMax(),
                "hue slider", false);
        try {
            hue.bindBidirectional(hueSlider.valueProperty());
            Bindings.bindBidirectional(hueTextField.textProperty(), hue, hueConverter);
            saturation.bindBidirectional(saturationSlider.valueProperty());
            Bindings.bindBidirectional(saturationTextField.textProperty(), saturation, saturationConverter);
            brightness.bindBidirectional(brightnessSlider.valueProperty());
            Bindings.bindBidirectional(brightnessTextField.textProperty(), brightness, saturationConverter);
        } catch (Exception e) {
            log.error(e);
        }
        /* Bind double properties to Sliders and TextFields*/

        /* Bind newColorPane's background to double properties */
        hue.addListener(((observable, oldValue, newValue) ->
                newColorPane.setBackground(
                        new Background(new BackgroundFill(
                                Color.hsb(
                                        newValue.doubleValue(),
                                        convertValue(saturation.doubleValue()),
                                        convertValue(brightness.doubleValue())),
                                CornerRadii.EMPTY,
                                Insets.EMPTY
                        ))
                )
        ));
        saturation.addListener(((observable, oldValue, newValue) ->
                newColorPane.setBackground(
                        new Background(new BackgroundFill(
                                Color.hsb(
                                        hue.getValue(),
                                        convertValue(newValue.doubleValue()),
                                        convertValue(brightness.doubleValue())),
                                CornerRadii.EMPTY,
                                Insets.EMPTY
                        ))
                )
        ));
        brightness.addListener(((observable, oldValue, newValue) ->
                newColorPane.setBackground(
                        new Background(new BackgroundFill(
                                Color.hsb(
                                        hue.getValue(),
                                        convertValue(saturation.doubleValue()),
                                        convertValue(newValue.doubleValue())),
                                CornerRadii.EMPTY,
                                Insets.EMPTY
                        ))
                )
        ));
        /* Bind newColorPane's background to double properties */
    }

    @FXML
    private void handleSavingColor() {
        colorProperty.setValue(Color.hsb(
                hue.doubleValue(),
                convertValue(saturation.doubleValue()),
                convertValue(brightness.doubleValue())));
    }

    private double convertValue(double convertibleValue) {
        return convertibleValue / 100d;
    }
}
