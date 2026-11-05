package com.jigneshgaming.fluxclient.mixin;

import com.jigneshgaming.fluxclient.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void flux$interceptZoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window != this.client.getWindow().getHandle()) {
            return;
        }
        if (this.client.currentScreen == null && this.client.getOverlay() == null && this.client.player != null) {
            if (ModuleManager.getInstance().zoom.onScroll(vertical)) {
                ci.cancel();
            }
        }
    }

    @Redirect(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
            )
    )
    private void flux$scaleZoomSensitivity(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY) {
        double mult = ModuleManager.getInstance().zoom.getSensitivityMultiplier();
        player.changeLookDirection(cursorDeltaX * mult, cursorDeltaY * mult);
    }
}
