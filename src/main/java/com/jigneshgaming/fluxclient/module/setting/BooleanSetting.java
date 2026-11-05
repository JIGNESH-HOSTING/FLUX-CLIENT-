package com.jigneshgaming.fluxclient.module.setting;

public final class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String key, String name, String description, boolean defaultValue) {
        super(key, name, description, defaultValue);
    }

    public boolean get() {
        return Boolean.TRUE.equals(value);
    }

    public void toggle() {
        this.value = !get();
    }
}
