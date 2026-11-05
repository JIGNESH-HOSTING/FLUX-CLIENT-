package com.jigneshgaming.fluxclient.module.setting;

import com.jigneshgaming.fluxclient.util.ColorUtil;

import java.util.Arrays;
import java.util.List;

public final class ColorSetting extends Setting<String> {
    public static final List<String> PRESETS = Arrays.asList(
            "Flux Blue",
            "Flux Orange",
            "White",
            "Emerald",
            "Crimson",
            "Gold",
            "Cyan",
            "Purple",
            "Chroma"
    );

    public ColorSetting(String key, String name, String description, String defaultPreset) {
        super(key, name, description, PRESETS.contains(defaultPreset) ? defaultPreset : "Flux Blue");
    }

    @Override
    public void setValue(String val) {
        if (val != null) {
            for (String p : PRESETS) {
                if (p.equalsIgnoreCase(val)) {
                    this.value = p;
                    return;
                }
            }
        }
    }

    public void cycleNext() {
        int idx = PRESETS.indexOf(value);
        this.value = PRESETS.get((idx + 1) % PRESETS.size());
    }

    public boolean is(String candidate) {
        return value != null && value.equalsIgnoreCase(candidate);
    }

    public int resolveArgb(int alpha) {
        int rgb = switch (value == null ? "Flux Blue" : value) {
            case "Flux Orange" -> 0xEE5A24;
            case "White" -> 0xF2F6FA;
            case "Emerald" -> 0x2ECC71;
            case "Crimson" -> 0xE74C3C;
            case "Gold" -> 0xF1C40F;
            case "Cyan" -> 0x00D2D3;
            case "Purple" -> 0x9B59B6;
            case "Chroma" -> ColorUtil.chroma(0L, 4.0f) & 0x00FFFFFF;
            default -> 0x2EA8FF;
        };
        return ColorUtil.withAlpha(rgb, alpha);
    }
}
