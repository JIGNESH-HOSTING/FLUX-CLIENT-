package com.jigneshgaming.fluxclient.module.impl.player;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;

public final class ToggleSneakModule extends Module {
    private final BooleanSetting releaseInGuiSetting;
    private boolean sneakToggled = false;
    private boolean wasSneakKeyDown = false;

    public ToggleSneakModule() {
        super(
                "toggle_sneak",
                "Toggle Sneak",
                "Lets you tap your Sneak key once to stay crouched hands-free.",
                ModuleCategory.PLAYER,
                ModuleIcon.TOGGLE_SNEAK,
                false
        );
        this.releaseInGuiSetting = addSetting(new BooleanSetting("pauseInGui", "Pause In Menus", "Temporarily unsneak while inventory/menus are open", true));
    }

    @Override
    protected void onDisable() {
        sneakToggled = false;
        wasSneakKeyDown = false;
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || client.player == null) {
            sneakToggled = false;
            wasSneakKeyDown = false;
            return;
        }
        if (client.currentScreen == null) {
            boolean down = client.options.sneakKey.isPressed();
            if (down && !wasSneakKeyDown) {
                sneakToggled = !sneakToggled;
            }
            wasSneakKeyDown = down;
        } else {
            wasSneakKeyDown = false;
        }
    }

    public boolean shouldApplySneak(MinecraftClient client) {
        if (!isEnabled() || !sneakToggled || client.player == null) {
            return false;
        }
        if (client.player.getAbilities().flying) {
            return false;
        }
        if (client.currentScreen != null && releaseInGuiSetting.get()) {
            return false;
        }
        return true;
    }

    public boolean isSneakToggled() {
        return isEnabled() && sneakToggled;
    }
}
