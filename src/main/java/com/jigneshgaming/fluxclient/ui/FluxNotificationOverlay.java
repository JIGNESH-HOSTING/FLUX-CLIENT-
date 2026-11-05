package com.jigneshgaming.fluxclient.ui;

import com.jigneshgaming.fluxclient.module.ModuleManager;
import com.jigneshgaming.fluxclient.module.impl.utility.NotificationsModule;
import com.jigneshgaming.fluxclient.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class FluxNotificationOverlay {
    public enum Type {
        INFO(0xFF2EA8FF),
        SUCCESS(0xFF2ECC71),
        WARNING(0xFFEE5A24);

        final int color;

        Type(int color) {
            this.color = color;
        }
    }

    private record ToastItem(String title, String message, Type type, long createdAtMs, long durationMs) {
    }

    private static final ConcurrentLinkedDeque<ToastItem> TOASTS = new ConcurrentLinkedDeque<>();
    private static boolean suppressNotifications = false;

    private FluxNotificationOverlay() {
    }

    public static void setSuppressed(boolean suppressed) {
        suppressNotifications = suppressed;
    }

    public static void push(String title, String message, Type type) {
        if (suppressNotifications) {
            return;
        }
        NotificationsModule mod = ModuleManager.getInstance() != null
                ? ModuleManager.getInstance().getModule(NotificationsModule.class)
                : null;
        if (mod != null && !mod.isEnabled()) {
            return;
        }
        long durationMs = mod != null ? (long) (mod.getDurationSeconds() * 1000.0) : 2500L;
        while (TOASTS.size() >= 4) {
            TOASTS.pollFirst();
        }
        TOASTS.addLast(new ToastItem(title, message, type, System.currentTimeMillis(), durationMs));
    }

    public static void clear() {
        TOASTS.clear();
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (TOASTS.isEmpty() || client == null || client.textRenderer == null) {
            return;
        }
        NotificationsModule mod = ModuleManager.getInstance() != null
                ? ModuleManager.getInstance().getModule(NotificationsModule.class)
                : null;
        if (mod != null && !mod.isEnabled()) {
            TOASTS.clear();
            return;
        }

        long now = System.currentTimeMillis();
        TextRenderer font = client.textRenderer;
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();

        int yCursor = sh - 34;
        Iterator<ToastItem> it = TOASTS.descendingIterator();
        while (it.hasNext()) {
            ToastItem item = it.next();
            long age = now - item.createdAtMs();
            if (age >= item.durationMs()) {
                TOASTS.remove(item);
                continue;
            }
            float slide = 1.0f;
            if (age < 180L) {
                slide = age / 180.0f;
            } else if (age > item.durationMs() - 220L) {
                slide = (item.durationMs() - age) / 220.0f;
            }
            slide = Math.max(0.0f, Math.min(1.0f, slide));
            float eased = 1.0f - (1.0f - slide) * (1.0f - slide);

            int textW = Math.max(font.getWidth(item.title()), font.getWidth(item.message()));
            int boxW = Math.max(132, textW + 22);
            int boxH = 26;
            int x = sw - Math.round((boxW + 8) * eased);
            int y = yCursor;

            int alpha = Math.round(225 * eased);
            context.fill(x, y, x + boxW, y + boxH, ColorUtil.argb(alpha, 12, 16, 22));
            context.drawStrokedRectangle(x, y, boxW, boxH, ColorUtil.argb(alpha, 38, 46, 58));
            context.fill(x, y, x + 3, y + boxH, ColorUtil.withAlpha(item.type().color, alpha));

            context.drawText(font, item.title(), x + 8, y + 4, ColorUtil.argb(Math.max(5, alpha), 245, 248, 252), true);
            context.drawText(font, item.message(), x + 8, y + 14, ColorUtil.argb(Math.max(5, Math.round(alpha * 0.85f)), 176, 186, 198), false);

            // Progress bar along bottom
            float rem = 1.0f - (age / (float) item.durationMs());
            int barW = Math.max(0, Math.round((boxW - 4) * rem));
            context.fill(x + 3, y + boxH - 2, x + 3 + barW, y + boxH - 1, ColorUtil.withAlpha(item.type().color, Math.round(alpha * 0.75f)));

            yCursor -= (boxH + 5);
        }
    }
}
