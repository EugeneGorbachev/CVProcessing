package ru.vsu.cvprocessing.settings;

import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

class ColorProperty extends ObjectPropertyBase<Color> {
    private static final Logger log = Logger.getLogger(ColorProperty.class);

    private String propertyName;

    ColorProperty(Color initialValue, String propertyName) {
        super(initialValue);
        this.propertyName = propertyName;
    }

    @Override
    public void set(Color newValue) {
        Color oldValue = get();
        super.set(newValue);
        log.info(String.format("%s value was changed from ", getName()) +
                String.format("H:%1.2f S:%1.2f B:%1.2f", oldValue.getHue(), oldValue.getSaturation(), oldValue.getBrightness()) +
                String.format(" to H:%1.2f S:%1.2f B:%1.2f", newValue.getHue(), newValue.getSaturation(), newValue.getBrightness()));
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public String getName() {
        return propertyName;
    }
}