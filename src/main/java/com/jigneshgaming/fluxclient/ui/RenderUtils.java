package com.jigneshgaming.fluxclient.ui;

import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.util.ColorUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * High-precision 2D UI rendering utilities and custom vector-pixel iconography
 * for FLUX CLIENT Main Menu and Mod Menu.
 */
public final class RenderUtils {
    private RenderUtils() {
    }

    public static boolean isHovered(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    /**
     * Draws a sleek chamfered/rounded rectangle (1px clipped corners) with 1px border.
     */
    public static void drawRoundedBox(DrawContext ctx, int x, int y, int w, int h, int fillColor, int borderColor) {
        if (w < 4 || h < 4) {
            ctx.fill(x, y, x + w, y + h, fillColor);
            return;
        }
        // Interior
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, fillColor);
        // Border edges excluding 4 corner pixels for clean rounded silhouette
        ctx.fill(x + 1, y, x + w - 1, y + 1, borderColor);
        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, borderColor);
        ctx.fill(x, y + 1, x + 1, y + h - 1, borderColor);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, borderColor);
    }

    /**
     * Draws a Main Menu button matching Screenshot 1 (obsidian normal state, vibrant electric-blue neon bloom on hover).
     */
    public static void drawMainMenuButton(DrawContext ctx, int x, int y, int w, int h, boolean hovered) {
        if (hovered) {
            // Multi-pass outer electric-blue neon bloom
            ctx.fill(x - 4, y - 3, x + w + 4, y + h + 3, 0x1E0D7BFF);
            ctx.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x3D188CFF);
            ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x702EA8FF);
            // Vibrant blue gradient interior
            ctx.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, 0xEE0C489E, 0xF20A66DB);
            // Crisp cyan-blue border
            ctx.fill(x + 1, y, x + w - 1, y + 1, 0xFF59C8FF);
            ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, 0xFF42B5FF);
            ctx.fill(x, y + 1, x + 1, y + h - 1, 0xFF4ABFFF);
            ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, 0xFF4ABFFF);
        } else {
            drawRoundedBox(ctx, x, y, w, h, 0xCC090D14, 0xFF28364A);
            // Subtle top highlight
            ctx.fill(x + 2, y + 1, x + w - 2, y + 2, 0x18FFFFFF);
        }
    }

    /**
     * Draws the Discord card on the bottom-right of the Main Menu with the signature blue bottom-right corner glow.
     */
    public static void drawDiscordCard(DrawContext ctx, int x, int y, int w, int h, boolean hovered) {
        // Bottom-right blue corner aura matching Screenshot 1
        int auraAlpha = hovered ? 0x55 : 0x33;
        ctx.fill(x + w - 34, y + h - 22, x + w + 3, y + h + 3, (auraAlpha << 24) | 0x188CFF);
        ctx.fill(x + w - 20, y + h - 12, x + w + 2, y + h + 2, ((auraAlpha + 0x22) << 24) | 0x2EA8FF);

        int border = hovered ? 0xFF3CAEFF : 0xFF233042;
        drawRoundedBox(ctx, x, y, w, h, 0xDE090D14, border);

        // Crisp electric-blue L-accent on bottom-right corner
        ctx.fill(x + w - 24, y + h - 1, x + w - 1, y + h, 0xFF38B2FF);
        ctx.fill(x + w - 1, y + h - 22, x + w, y + h - 1, 0xFF38B2FF);
    }

    public static String ellipsize(TextRenderer font, String text, int maxWidth) {
        if (text == null || font.getWidth(text) <= maxWidth) {
            return text == null ? "" : text;
        }
        String dots = "...";
        int avail = Math.max(0, maxWidth - font.getWidth(dots));
        return font.trimToWidth(text, avail) + dots;
    }

    // ==================== MAIN MENU & TOP BAR ICONS ====================

    public static void drawPersonIcon(DrawContext ctx, int cx, int cy, int color) {
        // Head
        ctx.fill(cx - 2, cy - 5, cx + 3, cy, color);
        ctx.fill(cx - 3, cy - 4, cx + 4, cy - 1, color);
        // Shoulders
        ctx.fill(cx - 4, cy + 2, cx + 5, cy + 6, color);
        ctx.fill(cx - 5, cy + 3, cx + 6, cy + 6, color);
    }

    public static void drawMultiplayerIcon(DrawContext ctx, int cx, int cy, int color) {
        int dim = ColorUtil.scaleAlpha(color, 0.78f);
        // Left & right side figures
        ctx.fill(cx - 6, cy - 3, cx - 3, cy, dim);
        ctx.fill(cx - 7, cy + 1, cx - 3, cy + 5, dim);
        ctx.fill(cx + 3, cy - 3, cx + 6, cy, dim);
        ctx.fill(cx + 3, cy + 1, cx + 7, cy + 5, dim);
        // Center figure
        ctx.fill(cx - 2, cy - 5, cx + 2, cy - 1, color);
        ctx.fill(cx - 4, cy + 1, cx + 4, cy + 6, color);
    }

    public static void drawHangerIcon(DrawContext ctx, int cx, int cy, int color) {
        // Hook
        ctx.fill(cx - 1, cy - 5, cx + 2, cy - 4, color);
        ctx.fill(cx + 1, cy - 4, cx + 2, cy - 2, color);
        ctx.fill(cx, cy - 2, cx + 1, cy, color);
        // Diagonal arms
        for (int i = 1; i <= 6; i++) {
            ctx.fill(cx - i, cy - 1 + (i / 2), cx - i + 1, cy + (i / 2) + 1, color);
            ctx.fill(cx + i, cy - 1 + (i / 2), cx + i + 1, cy + (i / 2) + 1, color);
        }
        // Bottom bar
        ctx.fill(cx - 6, cy + 3, cx + 7, cy + 4, color);
    }

    public static void drawPuzzleIcon(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 4, cy - 3, cx + 4, cy + 5, color);
        // Top knob
        ctx.fill(cx - 1, cy - 5, cx + 2, cy - 3, color);
        // Right knob
        ctx.fill(cx + 4, cy, cx + 6, cy + 3, color);
        // Left cutout
        ctx.fill(cx - 4, cy, cx - 2, cy + 2, 0xFF090D14);
    }

    public static void drawStoreIcon(DrawContext ctx, int cx, int cy, int color) {
        // Awning stripes
        ctx.fill(cx - 6, cy - 5, cx + 6, cy - 2, color);
        ctx.fill(cx - 5, cy - 2, cx - 3, cy, color);
        ctx.fill(cx - 1, cy - 2, cx + 1, cy, color);
        ctx.fill(cx + 3, cy - 2, cx + 5, cy, color);
        // Shop walls & door
        ctx.fill(cx - 5, cy, cx - 3, cy + 6, color);
        ctx.fill(cx + 3, cy, cx + 5, cy + 6, color);
        ctx.fill(cx - 5, cy + 4, cx + 5, cy + 6, color);
        ctx.fill(cx - 1, cy + 1, cx + 1, cy + 4, color);
    }

    public static void drawQuitDoorIcon(DrawContext ctx, int cx, int cy, int color) {
        // Door frame
        ctx.fill(cx - 5, cy - 5, cx + 1, cy - 4, color);
        ctx.fill(cx - 5, cy + 4, cx + 1, cy + 5, color);
        ctx.fill(cx - 5, cy - 5, cx - 4, cy + 5, color);
        // Exit arrow pointing right
        ctx.fill(cx - 2, cy - 1, cx + 5, cy + 1, color);
        ctx.fill(cx + 3, cy - 3, cx + 4, cy + 3, color);
        ctx.fill(cx + 4, cy - 2, cx + 5, cy + 2, color);
        ctx.fill(cx + 5, cy - 1, cx + 6, cy + 1, color);
    }

    public static void drawGridPlusIcon(DrawContext ctx, int cx, int cy, int color) {
        // 2x2 grid with '+' in bottom-right
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
        // 4 cardinal teeth
        ctx.fill(cx - 1, cy - 5, cx + 1, cy - 3, color);
        ctx.fill(cx - 1, cy + 3, cx + 1, cy + 5, color);
        ctx.fill(cx - 5, cy - 1, cx - 3, cy + 1, color);
        ctx.fill(cx + 3, cy - 1, cx + 5, cy + 1, color);
        // Diagonal teeth
        ctx.fill(cx - 4, cy - 4, cx - 2, cy - 2, color);
        ctx.fill(cx + 2, cy - 4, cx + 4, cy - 2, color);
        ctx.fill(cx - 4, cy + 2, cx - 2, cy + 4, color);
        ctx.fill(cx + 2, cy + 2, cx + 4, cy + 4, color);
        // Center hole
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
        // Discord controller silhouette (~24x18)
        ctx.fill(cx - 9, cy - 6, cx + 9, cy + 5, color);
        ctx.fill(cx - 11, cy - 4, cx + 11, cy + 4, color);
        ctx.fill(cx - 12, cy - 1, cx + 12, cy + 4, color);
        // Top ears
        ctx.fill(cx - 8, cy - 8, cx - 3, cy - 6, color);
        ctx.fill(cx + 3, cy - 8, cx + 8, cy - 6, color);
        // Bottom wings
        ctx.fill(cx - 10, cy + 4, cx - 5, cy + 7, color);
        ctx.fill(cx + 5, cy + 4, cx + 10, cy + 7, color);
        // Eyes
        ctx.fill(cx - 6, cy - 2, cx - 2, cy + 2, 0xFF090D14);
        ctx.fill(cx + 2, cy - 2, cx + 6, cy + 2, 0xFF090D14);
    }

    // ==================== 22 MODULE CARD ICONS ====================

    public static void drawModuleIcon(DrawContext ctx, TextRenderer font, ModuleIcon icon, int cx, int cy, int color) {
        switch (icon) {
            case FPS -> {
                ctx.drawStrokedRectangle(cx - 9, cy - 7, 19, 13, color);
                ctx.fill(cx - 2, cy + 6, cx + 3, cy + 8, color);
                ctx.fill(cx - 5, cy + 8, cx + 6, cy + 9, color);
                ctx.getMatrices().pushMatrix();
                ctx.getMatrices().translate(cx, cy - 3);
                ctx.getMatrices().scale(0.65f, 0.65f);
                ctx.drawCenteredTextWithShadow(font, "FPS", 0, 0, color);
                ctx.getMatrices().popMatrix();
            }
            case COORDINATES -> {
                // Y axis up, X/Z diagonal axes
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
                ctx.drawStrokedRectangle(cx - 6, cy - 6, 13, 13, color);
                for (int o = -4; o <= 4; o += 4) {
                    ctx.fill(cx + o, cy - 8, cx + o + 1, cy - 6, color);
                    ctx.fill(cx + o, cy + 7, cx + o + 1, cy + 9, color);
                    ctx.fill(cx - 8, cy + o, cx - 6, cy + o + 1, color);
                    ctx.fill(cx + 7, cy + o, cx + 9, cy + o + 1, color);
                }
                ctx.fill(cx - 3, cy - 2, cx + 4, cy + 3, ColorUtil.scaleAlpha(color, 0.7f));
            }
            case BIOME -> {
                // Pine tree + ground icon
                ctx.fill(cx - 1, cy - 8, cx + 2, cy - 5, color);
                ctx.fill(cx - 3, cy - 5, cx + 4, cy - 2, color);
                ctx.fill(cx - 5, cy - 2, cx + 6, cy + 2, color);
                ctx.fill(cx - 7, cy + 2, cx + 8, cy + 5, color);
                ctx.fill(cx - 1, cy + 5, cx + 2, cy + 9, color);
            }
            case RESOURCE_PACKS -> {
                ctx.drawStrokedRectangle(cx - 6, cy - 6, 13, 13, color);
                for (int i = -3; i <= 3; i++) {
                    ctx.fill(cx + i, cy - i - 1, cx + i + 2, cy - i + 1, color);
                }
                ctx.fill(cx - 4, cy + 2, cx - 1, cy + 5, color);
            }
            case SERVER_INFO -> {
                ctx.drawStrokedRectangle(cx - 4, cy - 7, 9, 9, color);
                ctx.fill(cx - 1, cy - 4, cx + 2, cy - 1, color);
                ctx.fill(cx - 1, cy + 2, cx + 2, cy + 6, color);
                ctx.fill(cx - 6, cy + 7, cx + 7, cy + 8, color);
            }
            case FULL_BRIGHT -> {
                ctx.fill(cx - 3, cy - 3, cx + 4, cy + 4, color);
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
                // Isometric 3D cube wireframe
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
                    ctx.fill(cx - 4 + i, cy - 5 + i, cx - 2 + i, cy - 3 + i, color);
                }
                ctx.fill(cx - 6, cy - 2, cx - 4, cy, color);
                ctx.fill(cx + 3, cy - 6, cx + 5, cy - 4, color);
                ctx.fill(cx - 2, cy + 4, cx, cy + 6, color);
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
                // Crescent moon
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
                ctx.fill(cx, cy - 6, cx + 3, cy - 3, color);
                ctx.fill(cx - 3, cy - 2, cx + 2, cy + 2, color);
                ctx.fill(cx - 5, cy + 2, cx - 1, cy + 6, color);
                ctx.fill(cx + 1, cy + 3, cx + 5, cy + 6, color);
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
                ctx.drawStrokedRectangle(cx - 6, cy - 5, 13, 12, color);
                ctx.fill(cx - 7, cy - 7, cx + 8, cy - 5, color);
                ctx.fill(cx - 3, cy - 1, cx + 4, cy + 1, ColorUtil.scaleAlpha(color, 0.75f));
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
