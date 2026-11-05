package com.jigneshgaming.fluxclient.ui;

import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.util.ColorUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Allocation-free 2D UI rendering utilities, smooth animation math, and custom iconography
 * for FLUX CLIENT Main Menu and Mod Menu.
 */
public final class RenderUtils {
    private RenderUtils() {
    }

    public static boolean isHovered(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    public static float animate(float current, boolean active, float deltaTicks, float speed) {
        float target = active ? 1.0f : 0.0f;
        if (Math.abs(current - target) < 0.004f) {
            return target;
        }
        float step = Math.min(1.0f, Math.max(0.02f, deltaTicks * speed));
        return current + (target - current) * step;
    }

    public static int lerpArgb(int c1, int c2, float t) {
        if (t <= 0.0f) return c1;
        if (t >= 1.0f) return c2;
        int a1 = (c1 >>> 24) & 0xFF, r1 = (c1 >>> 16) & 0xFF, g1 = (c1 >>> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF, r2 = (c2 >>> 16) & 0xFF, g2 = (c2 >>> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = Math.round(a1 + (a2 - a1) * t);
        int r = Math.round(r1 + (r2 - r1) * t);
        int g = Math.round(g1 + (g2 - g1) * t);
        int b = Math.round(b1 + (b2 - b1) * t);
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    /**
     * Draws a sleek chamfered/rounded rectangle (1px clipped corners) with 1px border.
     */
    public static void drawRoundedBox(DrawContext ctx, int x, int y, int w, int h, int fillColor, int borderColor) {
        if (w < 4 || h < 4) {
            ctx.fill(x, y, x + w, y + h, fillColor);
            return;
        }
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, fillColor);
        ctx.fill(x + 1, y, x + w - 1, y + 1, borderColor);
        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, borderColor);
        ctx.fill(x, y + 1, x + 1, y + h - 1, borderColor);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, borderColor);
    }

    /**
     * Smoothly animated Main Menu button with multi-pass electric-blue neon bloom on hover.
     */
    public static void drawMainMenuButton(DrawContext ctx, int x, int y, int w, int h, float hoverProgress) {
        float t = Math.max(0.0f, Math.min(1.0f, hoverProgress));
        if (t > 0.02f) {
            int outer1 = (Math.round(0x24 * t) << 24) | 0x0D7BFF;
            int outer2 = (Math.round(0x46 * t) << 24) | 0x188CFF;
            int outer3 = (Math.round(0x78 * t) << 24) | 0x2EA8FF;
            ctx.fill(x - 4, y - 3, x + w + 4, y + h + 3, outer1);
            ctx.fill(x - 2, y - 2, x + w + 2, y + h + 2, outer2);
            ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, outer3);
        }

        int topFill = lerpArgb(0xD2090D14, 0xF00B4A9E, t);
        int botFill = lerpArgb(0xD2090D14, 0xF50A6AE0, t);
        int border = lerpArgb(0xFF28364A, 0xFF56C6FF, t);

        ctx.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, topFill, botFill);
        ctx.fill(x + 1, y, x + w - 1, y + 1, border);
        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, border);
        ctx.fill(x, y + 1, x + 1, y + h - 1, border);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, border);
        ctx.fill(x + 2, y + 1, x + w - 2, y + 2, lerpArgb(0x16FFFFFF, 0x4088DCFF, t));
    }

    public static void drawMainMenuButton(DrawContext ctx, int x, int y, int w, int h, boolean hovered) {
        drawMainMenuButton(ctx, x, y, w, h, hovered ? 1.0f : 0.0f);
    }

    /**
     * Draws the Discord invitation card with bottom-right glowing electric-blue corner accent.
     */
    public static void drawDiscordCard(DrawContext ctx, int x, int y, int w, int h, float hoverProgress) {
        float t = Math.max(0.0f, Math.min(1.0f, hoverProgress));
        int aura1 = Math.round(0x32 + 0x2A * t);
        int aura2 = Math.round(0x52 + 0x30 * t);
        ctx.fill(x + w - 36, y + h - 24, x + w + 3, y + h + 3, (aura1 << 24) | 0x188CFF);
        ctx.fill(x + w - 22, y + h - 14, x + w + 2, y + h + 2, (aura2 << 24) | 0x2EA8FF);

        int border = lerpArgb(0xFF233042, 0xFF47B8FF, t);
        drawRoundedBox(ctx, x, y, w, h, 0xE4080C12, border);

        int cornerCol = lerpArgb(0xFF2DA8FF, 0xFF66CEFF, t);
        ctx.fill(x + w - 26, y + h - 1, x + w - 1, y + h, cornerCol);
        ctx.fill(x + w - 1, y + h - 22, x + w, y + h - 1, cornerCol);
    }

    public static String ellipsize(TextRenderer font, String text, int maxWidth) {
        if (text == null || font.getWidth(text) <= maxWidth) {
            return text == null ? "" : text;
        }
        String dots = "...";
        int avail = Math.max(0, maxWidth - font.getWidth(dots));
        return font.trimToWidth(text, avail) + dots;
    }

    // ==================== MAIN MENU & HEADER ICONS ====================

    public static void drawPersonIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 2, cy - 5, cx + 3, cy, color);
        ctx.fill(cx - 3, cy - 4, cx + 4, cy - 1, color);
        ctx.fill(cx - 4, cy + 1, cx + 5, cy + 5, color);
        ctx.fill(cx - 5, cy + 2, cx + 6, cy + 5, color);
    }

    public static void drawPersonGearIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 4, cy - 5, cx, cy - 1, color);
        ctx.fill(cx - 6, cy + 1, cx + 1, cy + 5, color);
        // Mini gear on bottom-right
        ctx.fill(cx + 2, cy, cx + 5, cy + 3, color);
        ctx.fill(cx + 3, cy - 1, cx + 4, cy + 4, color);
        ctx.fill(cx + 1, cy + 1, cx + 6, cy + 2, color);
    }

    public static void drawFourSquareIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 4, cy - 4, cx - 1, cy - 1, color);
        ctx.fill(cx + 1, cy - 4, cx + 4, cy - 1, color);
        ctx.fill(cx - 4, cy + 1, cx - 1, cy + 4, color);
        ctx.fill(cx + 1, cy + 1, cx + 4, cy + 4, color);
    }

    public static void drawSearchMagnifierIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.drawStrokedRectangle(cx - 4, cy - 4, 6, 6, color);
        ctx.fill(cx + 1, cy + 1, cx + 3, cy + 3, color);
        ctx.fill(cx + 2, cy + 2, cx + 4, cy + 4, color);
    }

    public static void drawMultiplayerIcon(DrawContext ctx, int cx, int cy, int color) {
        int dim = ColorUtil.scaleAlpha(color, 0.78f);
        ctx.fill(cx - 6, cy - 3, cx - 3, cy, dim);
        ctx.fill(cx - 7, cy + 1, cx - 3, cy + 5, dim);
        ctx.fill(cx + 3, cy - 3, cx + 6, cy, dim);
        ctx.fill(cx + 3, cy + 1, cx + 7, cy + 5, dim);
        ctx.fill(cx - 2, cy - 5, cx + 2, cy - 1, color);
        ctx.fill(cx - 4, cy + 1, cx + 4, cy + 5, color);
    }

    public static void drawHangerIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 1, cy - 5, cx + 2, cy - 4, color);
        ctx.fill(cx + 1, cy - 4, cx + 2, cy - 2, color);
        ctx.fill(cx, cy - 2, cx + 1, cy, color);
        for (int i = 1; i <= 6; i++) {
            ctx.fill(cx - i, cy - 1 + (i / 2), cx - i + 1, cy + (i / 2) + 1, color);
            ctx.fill(cx + i, cy - 1 + (i / 2), cx + i + 1, cy + (i / 2) + 1, color);
        }
        ctx.fill(cx - 6, cy + 3, cx + 7, cy + 4, color);
    }

    public static void drawPuzzleIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 4, cy - 3, cx + 4, cy + 5, color);
        ctx.fill(cx - 1, cy - 5, cx + 2, cy - 3, color);
        ctx.fill(cx + 4, cy, cx + 6, cy + 3, color);
        ctx.fill(cx - 4, cy, cx - 2, cy + 2, 0xFF090D14);
    }

    public static void drawStoreIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 6, cy - 5, cx + 6, cy - 2, color);
        ctx.fill(cx - 5, cy - 2, cx - 3, cy, color);
        ctx.fill(cx - 1, cy - 2, cx + 1, cy, color);
        ctx.fill(cx + 3, cy - 2, cx + 5, cy, color);
        ctx.fill(cx - 5, cy, cx - 3, cy + 5, color);
        ctx.fill(cx + 3, cy, cx + 5, cy + 5, color);
        ctx.fill(cx - 5, cy + 4, cx + 5, cy + 5, color);
        ctx.fill(cx - 1, cy + 1, cx + 1, cy + 4, color);
    }

    public static void drawQuitDoorIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 5, cy - 5, cx + 1, cy - 4, color);
        ctx.fill(cx - 5, cy + 4, cx + 1, cy + 5, color);
        ctx.fill(cx - 5, cy - 5, cx - 4, cy + 5, color);
        ctx.fill(cx - 2, cy - 1, cx + 5, cy + 1, color);
        ctx.fill(cx + 3, cy - 3, cx + 4, cy + 3, color);
        ctx.fill(cx + 4, cy - 2, cx + 5, cy + 2, color);
    }

    public static void drawRunningExitIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx + 1, cy - 5, cx + 3, cy - 3, color);
        ctx.fill(cx - 1, cy - 2, cx + 2, cy + 1, color);
        ctx.fill(cx - 4, cy - 1, cx - 1, cy, color);
        ctx.fill(cx + 2, cy - 1, cx + 4, cy + 1, color);
        ctx.fill(cx - 3, cy + 1, cx, cy + 4, color);
        ctx.fill(cx + 1, cy + 1, cx + 3, cy + 5, color);
    }

    public static void drawGridPlusIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 4, cy - 4, cx - 1, cy - 1, color);
        ctx.fill(cx + 1, cy - 4, cx + 4, cy - 1, color);
        ctx.fill(cx - 4, cy + 1, cx - 1, cy + 4, color);
        ctx.fill(cx + 2, cy + 1, cx + 3, cy + 5, color);
        ctx.fill(cx + 1, cy + 2, cx + 4, cy + 3, color);
    }

    public static void drawChatIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 5, cy - 4, cx + 5, cy + 2, color);
        ctx.fill(cx - 4, cy + 2, cx - 1, cy + 3, color);
        ctx.fill(cx - 4, cy + 3, cx - 3, cy + 5, color);
    }

    public static void drawCameraIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 2, cy - 5, cx + 2, cy - 3, color);
        ctx.fill(cx - 5, cy - 3, cx + 5, cy + 4, color);
        ctx.fill(cx - 2, cy - 1, cx + 2, cy + 2, 0xFF0D1117);
        ctx.fill(cx - 1, cy, cx + 1, cy + 1, color);
    }

    public static void drawGearIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 3, cy - 3, cx + 3, cy + 3, color);
        ctx.fill(cx - 1, cy - 5, cx + 1, cy - 3, color);
        ctx.fill(cx - 1, cy + 3, cx + 1, cy + 5, color);
        ctx.fill(cx - 5, cy - 1, cx - 3, cy + 1, color);
        ctx.fill(cx + 3, cy - 1, cx + 5, cy + 1, color);
        ctx.fill(cx - 4, cy - 4, cx - 2, cy - 2, color);
        ctx.fill(cx + 2, cy - 4, cx + 4, cy - 2, color);
        ctx.fill(cx - 4, cy + 2, cx - 2, cy + 4, color);
        ctx.fill(cx + 2, cy + 2, cx + 4, cy + 4, color);
        ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFF14181E);
    }

    public static void drawMonitorIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.drawStrokedRectangle(cx - 5, cy - 4, 11, 7, color);
        ctx.fill(cx - 1, cy + 3, cx + 2, cy + 5, color);
        ctx.fill(cx - 3, cy + 5, cx + 4, cy + 6, color);
    }

    public static void drawGaugeIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.drawStrokedRectangle(cx - 5, cy - 4, 11, 9, color);
        ctx.fill(cx, cy, cx + 3, cy + 1, color);
        ctx.fill(cx + 1, cy - 2, cx + 3, cy, color);
    }

    public static void drawDiscordLogo(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 9, cy - 6, cx + 9, cy + 5, color);
        ctx.fill(cx - 11, cy - 4, cx + 11, cy + 4, color);
        ctx.fill(cx - 12, cy - 1, cx + 12, cy + 4, color);
        ctx.fill(cx - 8, cy - 8, cx - 3, cy - 6, color);
        ctx.fill(cx + 3, cy - 8, cx + 8, cy - 6, color);
        ctx.fill(cx - 10, cy + 4, cx - 5, cy + 7, color);
        ctx.fill(cx + 5, cy + 4, cx + 10, cy + 7, color);
        ctx.fill(cx - 6, cy - 2, cx - 2, cy + 2, 0xFF090D14);
        ctx.fill(cx + 2, cy - 2, cx + 6, cy + 2, 0xFF090D14);
    }

    // ==================== 22 MODULE CARD ICONS ====================

    public static void drawModuleIcon(DrawContext ctx, TextRenderer font, ModuleIcon icon, int cx, int cy, int color) {
        switch (icon) {
            case FPS -> {
                ctx.drawStrokedRectangle(cx - 10, cy - 8, 21, 14, color);
                ctx.fill(cx - 2, cy + 6, cx + 3, cy + 8, color);
                ctx.fill(cx - 5, cy + 8, cx + 6, cy + 9, color);
                ctx.getMatrices().pushMatrix();
                ctx.getMatrices().translate(cx, cy - 6);
                ctx.getMatrices().scale(0.55f, 0.55f);
                ctx.drawCenteredTextWithShadow(font, "240", 0, 0, color);
                ctx.drawCenteredTextWithShadow(font, "FPS", 0, 9, color);
                ctx.getMatrices().popMatrix();
            }
            case COORDINATES -> {
                ctx.fill(cx, cy - 8, cx + 1, cy + 2, color);
                for (int i = 0; i < 7; i++) {
                    ctx.fill(cx - i, cy + 1 + (i / 2), cx - i + 1, cy + 2 + (i / 2), color);
                    ctx.fill(cx + i, cy + 1 + (i / 2), cx + i + 1, cy + 2 + (i / 2), color);
                }
                ctx.fill(cx - 1, cy - 9, cx + 2, cy - 7, color);
            }
            case DIRECTION -> {
                ctx.drawStrokedRectangle(cx - 7, cy - 7, 15, 15, color);
                ctx.fill(cx - 1, cy - 4, cx + 3, cy, color);
                ctx.fill(cx - 3, cy, cx + 1, cy + 4, ColorUtil.scaleAlpha(color, 0.65f));
            }
            case CLOCK -> {
                ctx.drawStrokedRectangle(cx - 6, cy - 5, 13, 13, color);
                ctx.fill(cx - 6, cy - 7, cx - 3, cy - 6, color);
                ctx.fill(cx + 4, cy - 7, cx + 7, cy - 6, color);
                ctx.fill(cx, cy - 3, cx + 1, cy + 2, color);
                ctx.fill(cx, cy + 1, cx + 4, cy + 2, color);
            }
            case MEMORY_USAGE -> {
                ctx.drawStrokedRectangle(cx - 7, cy - 7, 15, 15, color);
                for (int o = -4; o <= 4; o += 4) {
                    ctx.fill(cx + o, cy - 9, cx + o + 1, cy - 7, color);
                    ctx.fill(cx + o, cy + 8, cx + o + 1, cy + 10, color);
                    ctx.fill(cx - 9, cy + o, cx - 7, cy + o + 1, color);
                    ctx.fill(cx + 8, cy + o, cx + 10, cy + o + 1, color);
                }
                ctx.getMatrices().pushMatrix();
                ctx.getMatrices().translate(cx, cy - 2);
                ctx.getMatrices().scale(0.55f, 0.55f);
                ctx.drawCenteredTextWithShadow(font, "CPU", 0, 0, color);
                ctx.getMatrices().popMatrix();
            }
            case BIOME -> {
                // Stylized creature/biome silhouette matching reference
                ctx.fill(cx + 2, cy - 6, cx + 6, cy - 2, color);
                ctx.fill(cx - 6, cy - 2, cx + 4, cy + 3, color);
                ctx.fill(cx - 5, cy + 3, cx - 3, cy + 8, color);
                ctx.fill(cx + 2, cy + 3, cx + 4, cy + 8, color);
                ctx.fill(cx - 8, cy - 1, cx - 6, cy + 3, ColorUtil.scaleAlpha(color, 0.8f));
            }
            case RESOURCE_PACKS -> {
                ctx.drawStrokedRectangle(cx - 6, cy - 6, 13, 13, color);
                for (int i = -3; i <= 3; i++) {
                    ctx.fill(cx + i, cy - i - 1, cx + i + 2, cy - i + 1, color);
                }
                ctx.fill(cx - 4, cy + 2, cx - 1, cy + 5, color);
            }
            case SERVER_INFO -> {
                ctx.drawStrokedRectangle(cx - 5, cy - 8, 11, 11, color);
                ctx.getMatrices().pushMatrix();
                ctx.getMatrices().translate(cx, cy - 4);
                ctx.getMatrices().scale(0.55f, 0.55f);
                ctx.drawCenteredTextWithShadow(font, "IP", 0, 0, color);
                ctx.getMatrices().popMatrix();
                ctx.fill(cx - 1, cy + 3, cx + 2, cy + 6, color);
                ctx.fill(cx - 6, cy + 6, cx + 7, cy + 8, color);
            }
            case FULL_BRIGHT -> {
                ctx.drawStrokedRectangle(cx - 3, cy - 3, 7, 7, color);
                ctx.fill(cx, cy - 8, cx + 1, cy - 5, color);
                ctx.fill(cx, cy + 6, cx + 1, cy + 9, color);
                ctx.fill(cx - 8, cy, cx - 5, cy + 1, color);
                ctx.fill(cx + 6, cy, cx + 9, cy + 1, color);
                ctx.fill(cx - 6, cy - 6, cx - 4, cy - 4, color);
                ctx.fill(cx + 5, cy - 6, cx + 7, cy - 4, color);
                ctx.fill(cx - 6, cy + 5, cx - 4, cy + 7, color);
                ctx.fill(cx + 5, cy + 5, cx + 7, cy + 7, color);
            }
            case BLOCK_OVERLAY -> {
                ctx.drawStrokedRectangle(cx - 6, cy - 3, 10, 10, color);
                ctx.drawStrokedRectangle(cx - 2, cy - 7, 10, 10, color);
                ctx.fill(cx - 5, cy - 5, cx - 3, cy - 3, color);
                ctx.fill(cx + 4, cy - 5, cx + 6, cy - 3, color);
                ctx.fill(cx + 4, cy + 4, cx + 6, cy + 6, color);
            }
            case HITBOXES -> {
                ctx.drawStrokedRectangle(cx - 5, cy - 8, 11, 17, color);
                ctx.fill(cx - 2, cy - 6, cx + 3, cy - 2, color);
                ctx.fill(cx - 3, cy - 1, cx + 4, cy + 4, ColorUtil.scaleAlpha(color, 0.75f));
                ctx.fill(cx - 2, cy + 4, cx, cy + 8, color);
                ctx.fill(cx + 1, cy + 4, cx + 3, cy + 8, color);
            }
            case REMOVE_HURT_SHAKE -> {
                for (int i = 0; i < 6; i++) {
                    ctx.fill(cx - 2 + i, cy - 5 + i, cx + i, cy - 3 + i, color);
                }
                ctx.fill(cx - 6, cy - 2, cx - 4, cy, color);
                ctx.fill(cx + 3, cy - 6, cx + 5, cy - 4, color);
                ctx.fill(cx - 3, cy + 3, cx - 1, cy + 5, color);
            }
            case HIDE_BOSS_BAR -> {
                for (int h = -1; h <= 1; h++) {
                    int hx = cx + h * 6;
                    ctx.fill(hx - 2, cy, hx, cy + 2, color);
                    ctx.fill(hx + 1, cy, hx + 3, cy + 2, color);
                    ctx.fill(hx - 2, cy + 1, hx + 3, cy + 3, color);
                    ctx.fill(hx - 1, cy + 3, hx + 2, cy + 5, color);
                }
            }
            case TIME_CHANGER -> {
                ctx.fill(cx - 4, cy - 6, cx + 2, cy - 4, color);
                ctx.fill(cx - 6, cy - 4, cx - 2, cy + 4, color);
                ctx.fill(cx - 4, cy + 4, cx + 4, cy + 6, color);
                ctx.fill(cx + 2, cy + 2, cx + 6, cy + 4, color);
            }
            case CUSTOM_CROSSHAIR -> {
                ctx.drawStrokedRectangle(cx - 5, cy - 5, 11, 11, color);
                ctx.fill(cx, cy - 8, cx + 1, cy - 3, color);
                ctx.fill(cx, cy + 4, cx + 1, cy + 9, color);
                ctx.fill(cx - 8, cy, cx - 3, cy + 1, color);
                ctx.fill(cx + 4, cy, cx + 9, cy + 1, color);
                ctx.fill(cx, cy, cx + 1, cy + 1, color);
            }
            case CUSTOM_SCOREBOARD -> {
                ctx.drawStrokedRectangle(cx - 5, cy - 7, 11, 15, color);
                ctx.fill(cx - 3, cy - 4, cx + 3, cy - 3, color);
                ctx.fill(cx - 3, cy - 1, cx + 3, cy, color);
                ctx.fill(cx - 3, cy + 2, cx + 1, cy + 3, color);
                ctx.fill(cx - 3, cy + 5, cx + 2, cy + 6, color);
            }
            case AUTO_SPRINT -> {
                ctx.fill(cx + 1, cy - 8, cx + 4, cy - 5, color);
                ctx.fill(cx - 2, cy - 4, cx + 3, cy + 1, color);
                ctx.fill(cx - 5, cy - 3, cx - 2, cy - 1, color);
                ctx.fill(cx + 3, cy - 2, cx + 6, cy, color);
                ctx.fill(cx - 5, cy + 1, cx - 1, cy + 5, color);
                ctx.fill(cx + 1, cy + 1, cx + 5, cy + 7, color);
            }
            case TOGGLE_SNEAK -> {
                ctx.fill(cx + 1, cy - 6, cx + 4, cy - 3, color);
                ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, color);
                ctx.fill(cx - 5, cy - 1, cx - 2, cy + 1, color);
                ctx.fill(cx - 4, cy + 2, cx - 1, cy + 6, color);
                ctx.fill(cx + 1, cy + 3, cx + 4, cy + 7, color);
            }
            case ZOOM -> {
                ctx.drawStrokedRectangle(cx - 9, cy - 6, 19, 13, color);
                ctx.getMatrices().pushMatrix();
                ctx.getMatrices().translate(cx, cy - 2);
                ctx.getMatrices().scale(0.65f, 0.65f);
                ctx.drawCenteredTextWithShadow(font, "FOV", 0, 0, color);
                ctx.getMatrices().popMatrix();
            }
            case AUTO_GG, NOTIFICATIONS -> {
                ctx.drawStrokedRectangle(cx - 5, cy - 4, 11, 11, color);
                ctx.fill(cx - 6, cy - 6, cx + 7, cy - 4, color);
                ctx.fill(cx - 2, cy - 1, cx + 3, cy + 1, ColorUtil.scaleAlpha(color, 0.75f));
            }
            case QUICK_MESSAGES -> {
                ctx.drawStrokedRectangle(cx - 9, cy - 5, 19, 11, color);
                ctx.getMatrices().pushMatrix();
                ctx.getMatrices().translate(cx, cy - 2);
                ctx.getMatrices().scale(0.65f, 0.65f);
                ctx.drawCenteredTextWithShadow(font, "Aa|", 0, 0, color);
                ctx.getMatrices().popMatrix();
            }
        }
    }
}
