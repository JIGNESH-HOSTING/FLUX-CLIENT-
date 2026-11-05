package com.jigneshgaming.fluxclient.mixin;

import com.jigneshgaming.fluxclient.module.ModuleManager;
import com.jigneshgaming.fluxclient.module.impl.render.FullBrightModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Illuminates 0-light caves and night environments cleanly on Minecraft 1.21.11's
 * Blaze3D GPU Lightmap UBO pipeline without modifying server state.
 */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Unique
    private static final String STD140_PUT_FLOAT =
            "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;";

    @Shadow
    private boolean dirty;

    @Unique
    private float flux$lastSignature = -999.0f;

    @Inject(method = "update", at = @At("HEAD"))
    private void flux$refreshWhenToggled(float tickProgress, CallbackInfo ci) {
        FullBrightModule mod = ModuleManager.getInstance().fullBright;
        float sig = mod.isEnabled()
                ? mod.getEffectiveGamma() + (mod.shouldClearDarkness() ? 100.0f : 0.0f)
                : -1.0f;
        if (Math.abs(sig - this.flux$lastSignature) > 0.001f) {
            this.flux$lastSignature = sig;
            this.dirty = true;
        }
    }

    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = STD140_PUT_FLOAT, ordinal = 3), index = 0)
    private float flux$boostLightmapBrightness(float value) {
        FullBrightModule mod = ModuleManager.getInstance().fullBright;
        if (!mod.isEnabled()) {
            return value;
        }
        float ratio = Math.min(1.0f, Math.max(0.25f, (mod.getEffectiveGamma() - 1.0f) / 14.0f));
        return Math.max(value, ratio);
    }

    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = STD140_PUT_FLOAT, ordinal = 4), index = 0)
    private float flux$suppressDarknessScale(float value) {
        return ModuleManager.getInstance().fullBright.shouldClearDarkness() ? 0.0f : value;
    }

    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = STD140_PUT_FLOAT, ordinal = 5), index = 0)
    private float flux$suppressSkyDarkness(float value) {
        return ModuleManager.getInstance().fullBright.isEnabled() ? 0.0f : value;
    }

    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = STD140_PUT_FLOAT, ordinal = 6), index = 0)
    private float flux$boostGammaUniform(float value) {
        FullBrightModule mod = ModuleManager.getInstance().fullBright;
        return mod.isEnabled() ? Math.max(value, mod.getEffectiveGamma()) : value;
    }

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEffectFadeFactor(Lnet/minecraft/registry/entry/RegistryEntry;F)F"
            )
    )
    private float flux$suppressDarknessPulse(ClientPlayerEntity player, RegistryEntry<StatusEffect> effect, float tickProgress) {
        if (ModuleManager.getInstance().fullBright.shouldClearDarkness() && effect.equals(StatusEffects.DARKNESS)) {
            return 0.0f;
        }
        return player.getEffectFadeFactor(effect, tickProgress);
    }
}
