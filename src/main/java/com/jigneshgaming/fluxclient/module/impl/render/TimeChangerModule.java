package com.jigneshgaming.fluxclient.module.impl.render;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.ModeSetting;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;

public final class TimeChangerModule extends Module {
    private final ModeSetting presetSetting;
    private final NumberSetting customTicksSetting;

    public TimeChangerModule() {
        super(
                "time_changer",
                "Time Changer",
                "Overrides client-side sky and world time without sending packets to servers.",
                ModuleCategory.RENDER,
                ModuleIcon.TIME_CHANGER,
                false
        );
        this.presetSetting = addSetting(new ModeSetting(
                "preset",
                "Time Preset",
                "Sky time preset",
                "Day",
                "Sunrise",
                "Day",
                "Noon",
                "Sunset",
                "Night",
                "Midnight",
                "Custom"
        ));
        this.customTicksSetting = addSetting(new NumberSetting("customTicks", "Custom Time", "Ticks when preset is Custom (0-24000)", 6000, 0, 24000, 250, "t"));
    }

    public long getCustomTime() {
        return switch (presetSetting.getValue()) {
            case "Sunrise" -> 23000L;
            case "Day" -> 1000L;
            case "Noon" -> 6000L;
            case "Sunset" -> 12500L;
            case "Night" -> 14000L;
            case "Midnight" -> 18000L;
            default -> customTicksSetting.getInt();
        };
    }
}
