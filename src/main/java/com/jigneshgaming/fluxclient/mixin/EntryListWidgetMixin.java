package com.jigneshgaming.fluxclient.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces vanilla's tiled stone list texture on Multiplayer, World Selection, and Options lists
 * with a clean translucent obsidian glass panel and subtle electric-blue/slate hairline separators.
 */
@Mixin(EntryListWidget.class)
public abstract class EntryListWidgetMixin extends ContainerWidget {
    protected EntryListWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "drawMenuListBackground", at = @At("HEAD"), cancellable = true)
    private void flux$drawSleekListPanel(DrawContext ctx, CallbackInfo ci) {
        ctx.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0x80070A10);
        ci.cancel();
    }

    @Inject(method = "drawHeaderAndFooterSeparators", at = @At("HEAD"), cancellable = true)
    private void flux$drawCleanHairlineSeparators(DrawContext ctx, CallbackInfo ci) {
        ctx.fill(this.getX(), this.getY() - 1, this.getRight(), this.getY(), 0xAA253549);
        ctx.fill(this.getX(), this.getBottom(), this.getRight(), this.getBottom() + 1, 0xAA253549);
        ci.cancel();
    }
}
