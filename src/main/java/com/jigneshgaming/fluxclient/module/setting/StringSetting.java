package com.jigneshgaming.fluxclient.module.setting;

public final class StringSetting extends Setting<String> {
    private final int maxLength;

    public StringSetting(String key, String name, String description, String defaultValue, int maxLength) {
        super(key, name, description, defaultValue == null ? "" : defaultValue);
        this.maxLength = Math.max(1, maxLength);
    }

    @Override
    public void setValue(String val) {
        if (val != null) {
            this.value = val.length() > maxLength ? val.substring(0, maxLength) : val;
        }
    }

    public int getMaxLength() {
        return maxLength;
    }
}
