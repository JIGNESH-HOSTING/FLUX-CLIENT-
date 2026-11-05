package com.jigneshgaming.fluxclient.module.impl.render;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;

public final class FullBrightModule extends Module {
    private final NumberSetting strengthSetting;
    private final BooleanSetting clearDarknessSetting;

    public FullBrightModule() {
        super(
                "full_bright",
                "Full Bright",
                "Boosts world lightmap brightness so dark caves and night are clearly visible.",
                ModuleCategory.RENDER,
                ModuleIcon.FULL_BRIGHT,
                false
        );
        this.strengthSetting = addSetting(new NumberSetting("strength", "Brightness", "Full bright gamma multiplier", 100, 25, 100, 5, "%"));
        this.clearDarknessSetting = addSetting(new BooleanSetting("clearDarkness", "No Darkness Pulse", "Suppress warden/darkness dimming pulse", true));
    }

    public float getEffectiveGamma() {
        return 1.0f + (strengthSetting.getFloat() / 100.0f) * 14.0f;
    }

    public boolean shouldClearDarkness() {
        return isEnabled() && clearDarknessSetting.get();
    }
}
