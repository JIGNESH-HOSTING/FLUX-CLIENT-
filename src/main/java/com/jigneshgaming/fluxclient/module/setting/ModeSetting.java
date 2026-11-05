package com.jigneshgaming.fluxclient.module.setting;

import java.util.Arrays;
import java.util.List;

public final class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String key, String name, String description, String defaultMode, String... modes) {
        super(key, name, description, defaultMode);
        this.modes = Arrays.asList(modes);
        if (!this.modes.contains(defaultMode) && !this.modes.isEmpty()) {
            this.value = this.modes.get(0);
        }
    }

    @Override
    public void setValue(String val) {
        if (val != null) {
            for (String m : modes) {
                if (m.equalsIgnoreCase(val)) {
                    this.value = m;
                    return;
                }
            }
        }
    }

    public List<String> getModes() {
        return modes;
    }

    public boolean is(String mode) {
        return value != null && value.equalsIgnoreCase(mode);
    }

    public void cycleNext() {
        if (modes.isEmpty()) return;
        int idx = modes.indexOf(value);
        this.value = modes.get((idx + 1) % modes.size());
    }

    public void cyclePrevious() {
        if (modes.isEmpty()) return;
        int idx = modes.indexOf(value);
        this.value = modes.get((idx - 1 + modes.size()) % modes.size());
    }
}
