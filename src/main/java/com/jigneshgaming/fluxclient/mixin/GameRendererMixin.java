package com.jigneshgaming.fluxclient.mixin;

import com.jigneshgaming.fluxclient.module.ModuleManager;
import com.jigneshgaming.fluxclient.module.impl.render.RemoveHurtShakeModule;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void flux$applyZoomFov(Camera camera, float tickProgress, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (changingFov) {
            float updated = ModuleManager.getInstance().zoom.applyFov(cir.getReturnValue());
            cir.setReturnValue(updated);
        }
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void flux$cancelHurtShake(MatrixStack matrices, float tickProgress, CallbackInfo ci) {
        RemoveHurtShakeModule mod = ModuleManager.getInstance().removeHurtShake;
        if (mod.isEnabled() && mod.getHurtShakeFactor() <= 0.001f) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "tiltViewWhenHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"
            )
    )
    private Object flux$scaleDamageTiltStrength(SimpleOption<Double> option) {
        Double vanilla = option.getValue();
        RemoveHurtShakeModule mod = ModuleManager.getInstance().removeHurtShake;
        if (mod.isEnabled()) {
            return vanilla * (double) mod.getHurtShakeFactor();
        }
        return vanilla;
    }
}
