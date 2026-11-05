package com.jigneshgaming.fluxclient.module.impl.render;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;

public final class RemoveHurtShakeModule extends Module {
    private final NumberSetting hurtMultiplierSetting;

    public RemoveHurtShakeModule() {
        super(
                "remove_hurt_shake",
                "Remove Hurt Shake",
                "Eliminates or reduces screen camera tilt when taking combat damage.",
                ModuleCategory.RENDER,
                ModuleIcon.REMOVE_HURT_SHAKE,
                false
        );
        this.hurtMultiplierSetting = addSetting(new NumberSetting("intensity", "Shake Intensity", "0% completely disables hurt shake", 0, 0, 100, 5, "%"));
    }

    public float getHurtShakeFactor() {
        return hurtMultiplierSetting.getFloat() / 100.0f;
    }
}
