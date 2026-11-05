package com.jigneshgaming.fluxclient.mixin;

import com.jigneshgaming.fluxclient.module.ModuleManager;
import com.jigneshgaming.fluxclient.module.impl.render.TimeChangerModule;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.Properties.class)
public abstract class ClientWorldPropertiesMixin {

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void flux$overrideClientTime(CallbackInfoReturnable<Long> cir) {
        TimeChangerModule mod = ModuleManager.getInstance().timeChanger;
        if (mod.isEnabled()) {
            cir.setReturnValue(mod.getCustomTime());
        }
    }
}
