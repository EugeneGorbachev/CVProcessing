package ru.vsu.cvprocessing.settings;

public class HSBRange {
    private int colorHueStartRange;
    private int colorHueEndRange;
    private int colorSaturationStartRange;
    private int colorSaturationEndRange;
    private int colorBrightnessStartRange;
    private int colorBrightnessEndRange;

    private HSBRange() {
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

    public static Builder newBuilder() {
        return new HSBRange().new Builder();
    }

    public class Builder {
        private Builder() {
        }

        public Builder setColorHueStartRange(int colorHueStartRange) {
            HSBRange.this.colorBrightnessStartRange = colorHueStartRange;
            return this;
        }

        public Builder setColorHueEndRange(int colorHueEndRange) {
            HSBRange.this.colorHueEndRange = colorHueEndRange;
            return this;
        }

        public Builder setColorSaturationStartRange(int colorSaturationStartRange) {
            HSBRange.this.colorSaturationStartRange = colorSaturationStartRange;
            return this;
        }

        public Builder setColorSaturationEndRange(int colorSaturationEndRange) {
            HSBRange.this.colorSaturationEndRange = colorSaturationEndRange;
            return this;
        }

        public Builder setColorBrightnessStartRange(int colorBrightnessStartRange) {
            HSBRange.this.colorBrightnessStartRange = colorBrightnessStartRange;
            return this;
        }

        public Builder setColorBrightnessEndRange(int colorBrightnessEndRange) {
            HSBRange.this.colorBrightnessEndRange = colorBrightnessEndRange;
            return this;
        }

        public HSBRange build() {
            return HSBRange.this;
        }
    }
}
