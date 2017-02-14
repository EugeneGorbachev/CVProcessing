package ru.vsu.cvprocessing.event;

import org.springframework.context.ApplicationEvent;

public class ColorChangedEvent extends ApplicationEvent {
    private ColorType colorType;
    private Double hue;
    private Double saturation;
    private Double brightness;

    public ColorChangedEvent(Object source, ColorType colorType, Double hue, Double saturation, Double brightness) {
        super(source);
        this.colorType = colorType;
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public ColorType getColorType() {
        return colorType;
    }

    public Double getHue() {
        return hue;
    }

    public Double getSaturation() {
        return saturation;
    }

    public Double getBrightness() {
        return brightness;
    }
}
