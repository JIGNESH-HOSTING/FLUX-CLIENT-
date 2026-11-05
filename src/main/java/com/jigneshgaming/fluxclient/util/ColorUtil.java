package com.jigneshgaming.fluxclient.util;

/**
 * Allocation-free ARGB color utilities without any java.awt dependency
 * (critical for Android Java Edition launchers with stripped AWT modules).
 */
public final class ColorUtil {
    public static final int FLUX_BLUE = 0xFF2EA8FF;
    public static final int FLUX_BLUE_BRIGHT = 0xFF52C4FF;
    public static final int FLUX_BLUE_DARK = 0xFF0B4DB8;
    public static final int FLUX_ORANGE = 0xFFEE5A24;
    public static final int FLUX_ORANGE_HOVER = 0xFFFF6E38;
    public static final int FLUX_GREEN = 0xFF1E6F30;
    public static final int FLUX_GREEN_BORDER = 0xFF2FA048;

    private ColorUtil() {
    }

    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24)
                | ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | (b & 0xFF);
    }

    public static int withAlpha(int rgb, int alpha) {
        return (rgb & 0x00FFFFFF) | ((Math.max(0, Math.min(255, alpha)) & 0xFF) << 24);
    }

    public static int scaleAlpha(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int na = Math.max(0, Math.min(255, Math.round(a * factor)));
        return (argb & 0x00FFFFFF) | (na << 24);
    }

    public static int lerpColor(int c1, int c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int a1 = (c1 >>> 24) & 0xFF, r1 = (c1 >>> 16) & 0xFF, g1 = (c1 >>> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF, r2 = (c2 >>> 16) & 0xFF, g2 = (c2 >>> 8) & 0xFF, b2 = c2 & 0xFF;
        return argb(
                Math.round(a1 + (a2 - a1) * t),
                Math.round(r1 + (r2 - r1) * t),
                Math.round(g1 + (g2 - g1) * t),
                Math.round(b1 + (b2 - b1) * t)
        );
    }

    public static int chroma(long offsetMs, float speedSeconds) {
        float periodMs = Math.max(500.0f, speedSeconds * 1000.0f);
        float hue = ((System.currentTimeMillis() + offsetMs) % (long) periodMs) / periodMs;
        return hsvToArgb(hue, 0.80f, 1.0f, 255);
    }

    public static int hsvToArgb(float h, float s, float v, int alpha) {
        h = (h - (float) Math.floor(h)) * 6.0f;
        int i = (int) Math.floor(h);
        float f = h - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - s * f);
        float t = v * (1.0f - s * (1.0f - f));
        float r, g, b;
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return argb(alpha, Math.round(r * 255.0f), Math.round(g * 255.0f), Math.round(b * 255.0f));
    }
}
