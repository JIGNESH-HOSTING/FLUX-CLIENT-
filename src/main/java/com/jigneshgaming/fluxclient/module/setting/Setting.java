package com.jigneshgaming.fluxclient.module.setting;

public abstract class Setting<T> {
    private final String key;
    private final String name;
    private final String description;
    protected T value;
    protected final T defaultValue;

    protected Setting(String key, String name, String description, T defaultValue) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if (value != null) {
            this.value = value;
        }
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void reset() {
        this.value = this.defaultValue;
    }
}
