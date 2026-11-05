package com.jigneshgaming.fluxclient.mixin;

import com.jigneshgaming.fluxclient.FluxClientMod;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces vanilla's heavy 3D rotating panorama background on out-of-world screens
 * (Server List / MultiplayerScreen, World Select, Options) with the sleek dark FLUX backdrop.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Unique
    private static final Identifier FLUX_MENU_BG = Identifier.of(FluxClientMod.MOD_ID, "textures/gui/menu_bg.png");

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void flux$renderSleekDarkBackdrop(DrawContext ctx, float deltaTicks, CallbackInfo ci) {
        ctx.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                FLUX_MENU_BG,
                0, 0,
                0.0f, 0.0f,
                this.width, this.height,
                960, 540,
                960, 540,
                0xFFFFFFFF
        );
        ctx.fill(0, 0, this.width, this.height, 0xB8080B10);
        ci.cancel();
    }
}
