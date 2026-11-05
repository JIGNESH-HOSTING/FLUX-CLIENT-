package com.jigneshgaming.fluxclient.mixin;

import com.jigneshgaming.fluxclient.config.FluxConfigManager;
import com.jigneshgaming.fluxclient.optimization.LowEndOptimizer;
import com.jigneshgaming.fluxclient.ui.FluxMainMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public abstract boolean isWindowFocused();

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen flux$replaceVanillaTitleScreen(Screen screen) {
        if (screen instanceof TitleScreen) {
            return new FluxMainMenuScreen();
        }
        return screen;
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/InactivityFpsLimiter;update()I"
            )
    )
    private int flux$limitUnfocusedFps(InactivityFpsLimiter limiter) {
        int base = limiter.update();
        LowEndOptimizer opt = LowEndOptimizer.getInstance();
        if (!this.isWindowFocused() && opt.isUnfocusedFpsLimiter()) {
            return Math.min(base, opt.getUnfocusedMaxFps());
        }
        return base;
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void flux$saveConfigOnStop(CallbackInfo ci) {
        FluxConfigManager.saveSync();
    }
}
