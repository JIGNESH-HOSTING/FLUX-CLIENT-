package com.jigneshgaming.fluxclient.module.impl.movement;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;

public final class AutoSprintModule extends Module {
    private final BooleanSetting flyBoostCheckSetting;

    public AutoSprintModule() {
        super(
                "auto_sprint",
                "Auto Sprint",
                "Automatically sprints while moving forward using server-safe vanilla rules.",
                ModuleCategory.MOVEMENT,
                ModuleIcon.AUTO_SPRINT,
                true
        );
        this.flyBoostCheckSetting = addSetting(new BooleanSetting("allowFlying", "Sprint While Flying", "Maintain sprint state while creative flying", true));
    }

    public boolean shouldForceSprint(MinecraftClient client) {
        if (!isEnabled() || client == null) {
            return false;
        }
        ClientPlayerEntity player = client.player;
        if (player == null || client.currentScreen != null) {
            return false;
        }
        if (player.isSneaking() || player.isUsingItem() || player.horizontalCollision) {
            return false;
        }
        if (player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            return false;
        }
        if (player.getAbilities().flying && !flyBoostCheckSetting.get()) {
            return false;
        }
        boolean hasFood = player.getHungerManager().getFoodLevel() > 6 || player.getAbilities().allowFlying;
        return hasFood && player.input != null && player.input.hasForwardMovement();
    }
}
