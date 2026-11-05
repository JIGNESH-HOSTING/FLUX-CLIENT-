package com.jigneshgaming.fluxclient.module;

import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.ColorSetting;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;
import com.jigneshgaming.fluxclient.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public abstract class HudModule extends Module {
    private float xRatio;
    private float yRatio;
    private final float defaultXRatio;
    private final float defaultYRatio;
    private int lastRenderedWidth = 74;
    private int lastRenderedHeight = 18;

    protected final NumberSetting scaleSetting;
    protected final BooleanSetting backgroundSetting;
    protected final NumberSetting bgOpacitySetting;
    protected final BooleanSetting accentBarSetting;
    protected final BooleanSetting shadowSetting;
    protected final ColorSetting textColorSetting;

    protected HudModule(
            String id,
            String name,
            String description,
            ModuleIcon icon,
            boolean defaultEnabled,
            float defaultXRatio,
            float defaultYRatio
    ) {
        super(id, name, description, ModuleCategory.HUD, icon, defaultEnabled);
        this.defaultXRatio = clamp01(defaultXRatio);
        this.defaultYRatio = clamp01(defaultYRatio);
        this.xRatio = this.defaultXRatio;
        this.yRatio = this.defaultYRatio;

        this.scaleSetting = addSetting(new NumberSetting("scale", "Scale", "HUD widget scale", 1.0, 0.7, 1.8, 0.05, "x"));
        this.backgroundSetting = addSetting(new BooleanSetting("background", "Background", "Draw dark HUD card background", true));
        this.bgOpacitySetting = addSetting(new NumberSetting("bgOpacity", "BG Opacity", "HUD card background opacity", 155, 0, 235, 5, ""));
        this.accentBarSetting = addSetting(new BooleanSetting("accentBar", "Accent Line", "Draw subtle accent line on HUD card", true));
        this.shadowSetting = addSetting(new BooleanSetting("textShadow", "Text Shadow", "Render crisp drop shadow on text", true));
        this.textColorSetting = addSetting(new ColorSetting("textColor", "Accent Color", "Text and accent color", "White"));
    }

    private static float clamp01(float v) {
        return Math.max(0.005f, Math.min(0.98f, v));
    }

    public float getXRatio() {
        return xRatio;
    }

    public float getYRatio() {
        return yRatio;
    }

    public void setPositionRatio(float xRatio, float yRatio) {
        this.xRatio = clamp01(xRatio);
        this.yRatio = clamp01(yRatio);
    }

    public void resetPosition() {
        this.xRatio = this.defaultXRatio;
        this.yRatio = this.defaultYRatio;
    }

    @Override
    public void restoreDefaults() {
        super.restoreDefaults();
        resetPosition();
    }

    public float getScale() {
        return scaleSetting.getFloat();
    }

    public void setScale(double scale) {
        scaleSetting.setValue(scale);
    }

    public int getScreenX(int screenWidth) {
        int scaledW = Math.round(lastRenderedWidth * getScale());
        int rawX = Math.round(xRatio * screenWidth);
        return Math.max(2, Math.min(Math.max(2, screenWidth - scaledW - 2), rawX));
    }

    public int getScreenY(int screenHeight) {
        int scaledH = Math.round(lastRenderedHeight * getScale());
        int rawY = Math.round(yRatio * screenHeight);
        return Math.max(2, Math.min(Math.max(2, screenHeight - scaledH - 2), rawY));
    }

    public int resolvePixelX(int screenWidth) {
        return getScreenX(screenWidth);
    }

    public int resolvePixelY(int screenHeight) {
        return getScreenY(screenHeight);
    }

    public void setPixelPosition(int pixelX, int pixelY, int screenWidth, int screenHeight) {
        float rx = screenWidth > 0 ? (float) pixelX / (float) screenWidth : this.defaultXRatio;
        float ry = screenHeight > 0 ? (float) pixelY / (float) screenHeight : this.defaultYRatio;
        setPositionRatio(rx, ry);
    }

    public final void renderHud(DrawContext context, MinecraftClient client, int screenWidth, int screenHeight, boolean previewMode) {
        renderHudElement(context, client, previewMode);
    }

    public int getScaledWidth() {
        return Math.max(20, Math.round(lastRenderedWidth * getScale()));
    }

    public int getScaledHeight() {
        return Math.max(12, Math.round(lastRenderedHeight * getScale()));
    }

    protected void setLastDimensions(int width, int height) {
        this.lastRenderedWidth = Math.max(20, width);
        this.lastRenderedHeight = Math.max(12, height);
    }

    public final void renderHudElement(DrawContext context, MinecraftClient client, boolean previewMode) {
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int drawX = getScreenX(sw);
        int drawY = getScreenY(sh);
        float scale = getScale();

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(drawX, drawY);
        if (Math.abs(scale - 1.0f) > 0.001f) {
            context.getMatrices().scale(scale, scale);
        }
        try {
            renderContent(context, client, previewMode);
        } catch (Throwable ignored) {
        } finally {
            context.getMatrices().popMatrix();
        }
    }

    protected void drawHudLinesBox(DrawContext context, TextRenderer font, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        int maxTextW = 0;
        for (String line : lines) {
            int w = font.getWidth(line);
            if (w > maxTextW) {
                maxTextW = w;
            }
        }
        int padX = 6;
        int padY = 4;
        int lineGap = 11;
        int boxW = maxTextW + padX * 2;
        int boxH = padY * 2 + (lines.size() - 1) * lineGap + 8;
        setLastDimensions(boxW, boxH);

        if (backgroundSetting.get()) {
            int bgAlpha = bgOpacitySetting.getInt();
            context.fill(0, 0, boxW, boxH, ColorUtil.argb(bgAlpha, 11, 14, 19));
            context.drawStrokedRectangle(0, 0, boxW, boxH, ColorUtil.argb(Math.min(200, bgAlpha + 35), 34, 43, 56));
            if (accentBarSetting.get()) {
                int accent = textColorSetting.is("White")
                        ? ColorUtil.FLUX_BLUE
                        : textColorSetting.resolveArgb(235);
                context.fill(0, 0, 2, boxH, accent);
            }
        }

        int textColor = textColorSetting.resolveArgb(255);
        boolean shadow = shadowSetting.get();
        for (int i = 0; i < lines.size(); i++) {
            context.drawText(font, lines.get(i), padX + (accentBarSetting.get() && backgroundSetting.get() ? 1 : 0), padY + i * lineGap, textColor, shadow);
        }
    }

    protected abstract void renderContent(DrawContext context, MinecraftClient client, boolean previewMode);
}
