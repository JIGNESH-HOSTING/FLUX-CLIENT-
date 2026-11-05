package com.jigneshgaming.fluxclient.module.setting;

import java.util.Locale;

public final class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final String suffix;

    public NumberSetting(String key, String name, String description, double defaultValue, double min, double max, double step, String suffix) {
        super(key, name, description, clampValue(defaultValue, min, max, step));
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix == null ? "" : suffix;
    }

    private static double clampValue(double v, double min, double max, double step) {
        double clamped = Math.max(min, Math.min(max, v));
        if (step > 0.0) {
            clamped = Math.round(clamped / step) * step;
            clamped = Math.max(min, Math.min(max, clamped));
        }
        return clamped;
    }

    @Override
    public void setValue(Double val) {
        if (val != null && Double.isFinite(val)) {
            this.value = clampValue(val, min, max, step);
        }
    }

    public double get() {
        return value;
    }

    public float getFloat() {
        return value.floatValue();
    }

    public int getInt() {
        return (int) Math.round(value);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public float getNormalized() {
        if (max <= min) return 0.0f;
        return (float) ((value - min) / (max - min));
    }

    public void setFromNormalized(float normalized) {
        float clamped = Math.max(0.0f, Math.min(1.0f, normalized));
        setValue(min + clamped * (max - min));
    }

    public String formatValue() {
        if (step >= 1.0) {
            return String.format(Locale.ROOT, "%d%s", getInt(), suffix);
        } else if (step >= 0.1) {
            return String.format(Locale.ROOT, "%.1f%s", value, suffix);
        }
        return String.format(Locale.ROOT, "%.2f%s", value, suffix);
    }
}
