package ru.vsu.cvprocessing.settings;

public class HSBRange {
    private int colorHueStartRange;
    private int colorHueEndRange;
    private int colorSaturationStartRange;
    private int colorSaturationEndRange;
    private int colorBrightnessStartRange;
    private int colorBrightnessEndRange;

    HSBRange(int colorHueStartRange,  int colorHueEndRange,
                    int colorSaturationStartRange, int colorSaturationEndRange,
                    int colorBrightnessStartRange, int colorBrightnessEndRange) {
        this.colorHueStartRange = colorHueStartRange;
        this.colorSaturationStartRange = colorSaturationStartRange;
        this.colorBrightnessStartRange = colorBrightnessStartRange;
        this.colorHueEndRange = colorHueEndRange;
        this.colorSaturationEndRange = colorSaturationEndRange;
        this.colorBrightnessEndRange = colorBrightnessEndRange;
    }

    public void setColorHueRange(int colorHueStartRange, int colorHueEndRange) {
        this.colorHueStartRange = colorHueStartRange;
        this.colorHueEndRange = colorHueEndRange;
    }

    public void setColorSaturationRange(int colorSaturationStartRange, int colorSaturationEndRange) {
        this.colorSaturationStartRange = colorSaturationStartRange;
        this.colorSaturationEndRange = colorSaturationEndRange;
    }

    public void setColorBrightnessRange(int colorBrightnessStartRange, int colorBrightnessEndRange) {
        this.colorBrightnessStartRange = colorBrightnessStartRange;
        this.colorBrightnessEndRange = colorBrightnessEndRange;
    }

    public int getColorHueStartRange() {
        return colorHueStartRange;
    }

    public int getColorHueEndRange() {
        return colorHueEndRange;
    }

    public int getColorSaturationStartRange() {
        return colorSaturationStartRange;
    }

    public int getColorSaturationEndRange() {
        return colorSaturationEndRange;
    }

    public int getColorBrightnessStartRange() {
        return colorBrightnessStartRange;
    }

    public int getColorBrightnessEndRange() {
        return colorBrightnessEndRange;
    }

    public void setColorHueStartRange(int colorHueStartRange) {
        this.colorHueStartRange = colorHueStartRange;
    }

    public void setColorHueEndRange(int colorHueEndRange) {
        this.colorHueEndRange = colorHueEndRange;
    }

    public void setColorSaturationStartRange(int colorSaturationStartRange) {
        this.colorSaturationStartRange = colorSaturationStartRange;
    }

    public void setColorSaturationEndRange(int colorSaturationEndRange) {
        this.colorSaturationEndRange = colorSaturationEndRange;
    }

    public void setColorBrightnessStartRange(int colorBrightnessStartRange) {
        this.colorBrightnessStartRange = colorBrightnessStartRange;
    }

    public void setColorBrightnessEndRange(int colorBrightnessEndRange) {
        this.colorBrightnessEndRange = colorBrightnessEndRange;
    }
}
