package com.jigneshgaming.fluxclient.ui;

import com.jigneshgaming.fluxclient.config.FluxConfigManager;
import com.jigneshgaming.fluxclient.config.HudPresetManager;
import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.ModuleManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Interactive drag-and-drop & scroll-to-scale HUD layout editor for FLUX CLIENT.
 */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudModule draggingModule = null;
    private double dragOffsetX = 0.0;
    private double dragOffsetY = 0.0;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("Flux HUD Editor"));
        this.parent = parent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        HudPresetManager.captureCurrentAsCustomLayout();
        FluxConfigManager.saveAsync();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Lightweight translucent grid overlay rendered in render()
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x6606090E);

        // Subtle center alignment guides
        ctx.fill(this.width / 2, 0, this.width / 2 + 1, this.height, 0x33EE5A24);
        ctx.fill(0, this.height / 2, this.width, this.height / 2 + 1, 0x33EE5A24);

        List<HudModule> hudModules = ModuleManager.getInstance().getHudModules();
        for (HudModule mod : hudModules) {
            if (!mod.isEnabled()) {
                continue;
            }
            if (this.client != null) {
                mod.renderHud(ctx, this.client, this.width, this.height, true);
            }
            int x = mod.resolvePixelX(this.width);
            int y = mod.resolvePixelY(this.height);
            int w = mod.getScaledWidth();
            int h = mod.getScaledHeight();

            boolean hov = (draggingModule == mod) || RenderUtils.isHovered(mouseX, mouseY, x, y, w, h);
            int outline = (draggingModule == mod) ? 0xFFEE5A24 : (hov ? 0xFF38B2FF : 0x88FFFFFF);
            ctx.drawStrokedRectangle(x - 1, y - 1, w + 2, h + 2, outline);
        }

        // Top-center floating control bar
        int barW = 290;
        int barH = 38;
        int barX = (this.width - barW) / 2;
        int barY = 10;
        RenderUtils.drawRoundedBox(ctx, barX, barY, barW, barH, 0xEE0B0E13, 0xFF283240);
        ctx.drawCenteredTextWithShadow(
                this.textRenderer,
                "HUD LAYOUT EDITOR — Drag to Move | Scroll to Resize",
                barX + barW / 2,
                barY + 6,
                0xFFF2F6FA
        );

        int saveX = barX + 46;
        int btnY = barY + 19;
        boolean saveHov = RenderUtils.isHovered(mouseX, mouseY, saveX, btnY, 92, 15);
        RenderUtils.drawRoundedBox(ctx, saveX, btnY, 92, 15, saveHov ? 0xFFFF6B35 : 0xFFEE5A24, 0xFFFF7A48);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Save & Back", saveX + 46, btnY + 4, 0xFFFFFFFF);

        int resetX = saveX + 100;
        boolean resetHov = RenderUtils.isHovered(mouseX, mouseY, resetX, btnY, 96, 15);
        RenderUtils.drawRoundedBox(ctx, resetX, btnY, 96, 15, resetHov ? 0xFF242D39 : 0xFF171C24, 0xFF313C4B);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Reset Positions", resetX + 48, btnY + 4, 0xFFDCE6F2);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();

        int barW = 290;
        int barX = (this.width - barW) / 2;
        int btnY = 29;
        if (RenderUtils.isHovered(mx, my, barX + 46, btnY, 92, 15)) {
            this.close();
            return true;
        }
        if (RenderUtils.isHovered(mx, my, barX + 146, btnY, 96, 15)) {
            for (HudModule m : ModuleManager.getInstance().getHudModules()) {
                m.resetPosition();
            }
            FluxConfigManager.saveAsync();
            return true;
        }

        List<HudModule> hudModules = ModuleManager.getInstance().getHudModules();
        for (int i = hudModules.size() - 1; i >= 0; i--) {
            HudModule mod = hudModules.get(i);
            if (!mod.isEnabled()) {
                continue;
            }
            int x = mod.resolvePixelX(this.width);
            int y = mod.resolvePixelY(this.height);
            int w = mod.getScaledWidth();
            int h = mod.getScaledHeight();
            if (RenderUtils.isHovered(mx, my, x, y, w, h)) {
                if (click.button() == 1) {
                    mod.toggle();
                    FluxConfigManager.saveAsync();
                    return true;
                }
                draggingModule = mod;
                dragOffsetX = mx - x;
                dragOffsetY = my - y;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (draggingModule != null && click.button() == 0) {
            int newX = (int) Math.round(click.x() - dragOffsetX);
            int newY = (int) Math.round(click.y() - dragOffsetY);
            // Snap gently to center axes within 4px
            int centerW = (this.width - draggingModule.getScaledWidth()) / 2;
            if (Math.abs(newX - centerW) <= 4) {
                newX = centerW;
            }
            draggingModule.setPixelPosition(newX, newY, this.width, this.height);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (draggingModule != null) {
            draggingModule = null;
            HudPresetManager.captureCurrentAsCustomLayout();
            FluxConfigManager.saveAsync();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        List<HudModule> hudModules = ModuleManager.getInstance().getHudModules();
        for (int i = hudModules.size() - 1; i >= 0; i--) {
            HudModule mod = hudModules.get(i);
            if (!mod.isEnabled()) continue;
            int x = mod.resolvePixelX(this.width);
            int y = mod.resolvePixelY(this.height);
            if (RenderUtils.isHovered(mouseX, mouseY, x, y, mod.getScaledWidth(), mod.getScaledHeight())) {
                mod.setScale(mod.getScale() + (verticalAmount > 0 ? 0.05 : -0.05));
                FluxConfigManager.saveAsync();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
