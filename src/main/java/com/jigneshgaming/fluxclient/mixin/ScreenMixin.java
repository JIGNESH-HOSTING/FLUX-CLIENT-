package com.jigneshgaming.fluxclient.mixin;

import com.jigneshgaming.fluxclient.FluxClientMod;
import com.jigneshgaming.fluxclient.optimization.LowEndOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the custom dark desaturated FLUX voxel backdrop to Main Menu, MultiplayerScreen,
 * SelectWorldScreen, and OptionsScreen while preserving 100% of vanilla Minecraft functionality
 * and bypassing expensive Gaussian menu blur FBO passes on low-end GPUs.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Unique
    private static final Identifier FLUX_MENU_BG = Identifier.of(FluxClientMod.MOD_ID, "textures/gui/menu_bg.png");

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected MinecraftClient client;

    @Unique
    private boolean flux$isThemedMenuScreen() {
        Object self = this;
        return self instanceof MultiplayerScreen
                || self instanceof SelectWorldScreen
                || self instanceof OptionsScreen
                || self instanceof GameOptionsScreen;
    }

    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void flux$renderDarkFluxBackdrop(DrawContext ctx, float deltaTicks, CallbackInfo ci) {
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
        ctx.fillGradient(0, 0, this.width, this.height, 0x7A06090F, 0xAA05080D);
        ci.cancel();
    }

    @Inject(method = "applyBlur", at = @At("HEAD"), cancellable = true)
    private void flux$skipHeavyGaussianMenuBlur(DrawContext ctx, CallbackInfo ci) {
        if (LowEndOptimizer.getInstance().isMenuBlurBypass() || flux$isThemedMenuScreen()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderDarkening(Lnet/minecraft/client/gui/DrawContext;IIII)V", at = @At("HEAD"), cancellable = true)
    private void flux$renderCleanDarkening(DrawContext ctx, int x, int y, int w, int h, CallbackInfo ci) {
        if (flux$isThemedMenuScreen()) {
            if (this.client != null && this.client.world != null) {
                // In-game Options screen: render dark desaturated FLUX backdrop tint
                ctx.drawTexture(
                        RenderPipelines.GUI_TEXTURED,
                        FLUX_MENU_BG,
                        x, y,
                        0.0f, 0.0f,
                        w, h,
                        960, 540,
                        960, 540,
                        0xCCFFFFFF
                );
                ctx.fill(x, y, x + w, y + h, 0xB806090E);
            } else {
                ctx.fill(x, y, x + w, y + h, 0x4805080D);
            }
            ci.cancel();
        }
    }

    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    private void flux$styleInGameOptionsBackdrop(DrawContext ctx, CallbackInfo ci) {
        if (flux$isThemedMenuScreen()) {
            ctx.fillGradient(0, 0, this.width, this.height, 0xD4070A10, 0xE205080D);
            ci.cancel();
        }
    }
}
