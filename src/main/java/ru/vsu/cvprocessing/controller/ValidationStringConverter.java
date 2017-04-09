package ru.vsu.cvprocessing.controller;


import javafx.util.StringConverter;
import org.apache.log4j.Logger;

import java.util.function.Function;

class ValidationStringConverter extends StringConverter<Number> {
    private static final Logger log = Logger.getLogger(ValidationStringConverter.class);

    private Function<Number, Boolean> validator;
    private Number defaultValue;
    private String valueName;
    private boolean logging;

    ValidationStringConverter(Function<Number, Boolean> validator, Number defaultValue, String valueName, boolean logging) {
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.valueName = valueName;
        this.logging = logging;
    }

    @Override
    public String toString(Number object) {
        String string = Integer.toString(object.intValue());
        if (logging) {
            log.info(String.format("%s value was changed on %d", valueName, object.intValue()));
        }
        return string;
    }

    @Override
    public Number fromString(String string) {
        Integer integerValue;
        try {
            integerValue = Integer.parseInt(string);
            if (!validator.apply(integerValue)) {
                throw new IllegalArgumentException("Double value failed validation");
            }
        } catch (Exception e) {
            log.error(e);
            return defaultValue;
        }
        if (logging) {
            log.info(String.format("%s value was changed on %d", valueName, integerValue));
        }
        return integerValue;
    }
}