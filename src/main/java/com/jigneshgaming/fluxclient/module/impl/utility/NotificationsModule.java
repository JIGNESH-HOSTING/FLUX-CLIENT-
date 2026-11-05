package com.jigneshgaming.fluxclient.module.impl.utility;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;

public final class NotificationsModule extends Module {
    private final NumberSetting durationSetting;

    public NotificationsModule() {
        super(
                "notifications",
                "Notifications",
                "Shows clean corner toast alerts for module toggles, presets, and client events.",
                ModuleCategory.UTILITY,
                ModuleIcon.NOTIFICATIONS,
                true
        );
        this.durationSetting = addSetting(new NumberSetting("duration", "Toast Duration", "How long notifications stay visible", 2.5, 1.0, 6.0, 0.5, "s"));
    }

    public double getDurationSeconds() {
        return durationSetting.get();
    }
}
