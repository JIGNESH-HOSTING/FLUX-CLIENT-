package com.jigneshgaming.fluxclient.mixin;

import com.jigneshgaming.fluxclient.cosmetics.CosmeticsManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void flux$applyClientCosmeticCape(CallbackInfoReturnable<SkinTextures> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || (Object) this != client.player) {
            return;
        }
        AssetInfo.TextureAssetInfo capeAsset = CosmeticsManager.getInstance().getActiveCape().getAssetInfo();
        if (capeAsset == null) {
            return;
        }
        SkinTextures base = cir.getReturnValue();
        if (base != null) {
            SkinTextures.SkinOverride override = SkinTextures.SkinOverride.create(
                    Optional.empty(),
                    Optional.of(capeAsset),
                    Optional.of(capeAsset),
                    Optional.empty()
            );
            cir.setReturnValue(base.withOverride(override));
        }
    }
}
