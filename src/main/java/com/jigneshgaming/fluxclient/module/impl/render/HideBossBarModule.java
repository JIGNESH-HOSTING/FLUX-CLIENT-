package com.jigneshgaming.fluxclient.module.impl.render;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;

public final class HideBossBarModule extends Module {
    public HideBossBarModule() {
        super(
                "hide_boss_bar",
                "Hide Boss Bar",
                "Hides top-screen boss bars to keep PvP and server views clean on compact screens.",
                ModuleCategory.RENDER,
                ModuleIcon.HIDE_BOSS_BAR,
                false
        );
    }
}
