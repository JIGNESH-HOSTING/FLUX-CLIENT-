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
 * Interactive drag-and-drop & resize HUD layout editor for FLUX CLIENT.
 * Supports both mouse-wheel scaling and touch-friendly [-]/[+] scale buttons for PojavLauncher.
 */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudModule draggingModule = null;
    private HudModule selectedModule = null;
    private double dragOffsetX = 0.0;
    private double dragOffsetY = 0.0;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("Flux HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (selectedModule == null) {
            for (HudModule m : ModuleManager.getInstance().getHudModules()) {
                if (m.isEnabled()) {
                    selectedModule = m;
                    break;
                }
            }
        }
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
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x6606090E);

        // Center-axis alignment guides
        ctx.fill(this.width / 2, 0, this.width / 2 + 1, this.height, 0x38F05A24);
        ctx.fill(0, this.height / 2, this.width, this.height / 2 + 1, 0x38F05A24);

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

            boolean active = (draggingModule == mod) || (selectedModule == mod);
            boolean hov = RenderUtils.isHovered(mouseX, mouseY, x, y, w, h);
            int outline = (draggingModule == mod)
                    ? FluxModMenuScreen.ACCENT_ORANGE
                    : (active ? 0xFF38B2FF : (hov ? 0xCCFFFFFF : 0x77FFFFFF));
            ctx.drawStrokedRectangle(x - 1, y - 1, w + 2, h + 2, outline);
        }

        // Top-center floating control bar (with touch-friendly [-] / [+] scale buttons)
        int barW = Math.min(336, this.width - 16);
        int barH = 38;
        int barX = (this.width - barW) / 2;
        int barY = 8;
        RenderUtils.drawRoundedBox(ctx, barX, barY, barW, barH, 0xEE0B0E13, 0xFF283240);

        String headerText = selectedModule != null
                ? "HUD EDITOR — " + selectedModule.getName() + " (" + String.format("%.2fx", selectedModule.getScale()) + ")"
                : "HUD EDITOR — Drag Elements | Scroll or Tap -/+ to Resize";
        ctx.drawCenteredTextWithShadow(this.textRenderer, headerText, barX + barW / 2, barY + 5, 0xFFF2F6FA);

        int btnY = barY + 19;
        int minusX = barX + 12;
        boolean minusHov = RenderUtils.isHovered(mouseX, mouseY, minusX, btnY, 28, 15);
        RenderUtils.drawRoundedBox(ctx, minusX, btnY, 28, 15, minusHov ? 0xFF242D39 : 0xFF171C24, 0xFF313C4B);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "-", minusX + 14, btnY + 4, 0xFFFFFFFF);

        int plusX = minusX + 32;
        boolean plusHov = RenderUtils.isHovered(mouseX, mouseY, plusX, btnY, 28, 15);
        RenderUtils.drawRoundedBox(ctx, plusX, btnY, 28, 15, plusHov ? 0xFF242D39 : 0xFF171C24, 0xFF313C4B);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "+", plusX + 14, btnY + 4, 0xFFFFFFFF);

        int resetX = plusX + 36;
        boolean resetHov = RenderUtils.isHovered(mouseX, mouseY, resetX, btnY, 96, 15);
        RenderUtils.drawRoundedBox(ctx, resetX, btnY, 96, 15, resetHov ? 0xFF242D39 : 0xFF171C24, 0xFF313C4B);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Reset Positions", resetX + 48, btnY + 4, 0xFFDCE6F2);

        int saveW = barX + barW - 12 - (resetX + 102);
        int saveX = resetX + 102;
        boolean saveHov = RenderUtils.isHovered(mouseX, mouseY, saveX, btnY, saveW, 15);
        RenderUtils.drawRoundedBox(ctx, saveX, btnY, saveW, 15, saveHov ? FluxModMenuScreen.ACCENT_ORANGE_HOVER : FluxModMenuScreen.ACCENT_ORANGE, 0xFFFF7A48);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Save & Back", saveX + saveW / 2, btnY + 4, 0xFFFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();

        int barW = Math.min(336, this.width - 16);
        int barX = (this.width - barW) / 2;
        int btnY = 27;
        int minusX = barX + 12;
        int plusX = minusX + 32;
        int resetX = plusX + 36;
        int saveX = resetX + 102;
        int saveW = barX + barW - 12 - saveX;

        if (RenderUtils.isHovered(mx, my, minusX, btnY, 28, 15)) {
            if (selectedModule != null) {
                selectedModule.setScale(selectedModule.getScale() - 0.05);
                FluxConfigManager.saveAsync();
            }
            return true;
        }
        if (RenderUtils.isHovered(mx, my, plusX, btnY, 28, 15)) {
            if (selectedModule != null) {
                selectedModule.setScale(selectedModule.getScale() + 0.05);
                FluxConfigManager.saveAsync();
            }
            return true;
        }
        if (RenderUtils.isHovered(mx, my, resetX, btnY, 96, 15)) {
            for (HudModule m : ModuleManager.getInstance().getHudModules()) {
                m.resetPosition();
                m.setScale(1.0);
            }
            FluxConfigManager.saveAsync();
            return true;
        }
        if (RenderUtils.isHovered(mx, my, saveX, btnY, saveW, 15)) {
            this.close();
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
                selectedModule = mod;
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
            // Snap to horizontal & vertical center axes within 4px
            int centerW = (this.width - draggingModule.getScaledWidth()) / 2;
            int centerH = (this.height - draggingModule.getScaledHeight()) / 2;
            if (Math.abs(newX - centerW) <= 4) {
                newX = centerW;
            }
            if (Math.abs(newY - centerH) <= 4) {
                newY = centerH;
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
                selectedModule = mod;
                mod.setScale(mod.getScale() + (verticalAmount > 0 ? 0.05 : -0.05));
                FluxConfigManager.saveAsync();
                return true;
            }
        }
        if (selectedModule != null) {
            selectedModule.setScale(selectedModule.getScale() + (verticalAmount > 0 ? 0.05 : -0.05));
            FluxConfigManager.saveAsync();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
