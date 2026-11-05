package com.jigneshgaming.fluxclient.module.impl.hud;

import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Collections;
import java.util.Locale;

public final class MemoryUsageModule extends HudModule {
    private final BooleanSetting showMbSetting;
    private final BooleanSetting showBarSetting;

    private long lastUpdateMs = 0L;
    private String cachedText = "Mem: 35%";
    private float cachedRatio = 0.35f;

    public MemoryUsageModule() {
        super(
                "memory_usage",
                "Memory Usage",
                "Displays real JVM heap usage percentage and MB allocation.",
                ModuleIcon.MEMORY_USAGE,
                false,
                0.012f,
                0.155f
        );
        this.showMbSetting = addSetting(new BooleanSetting("showMb", "Show MB", "Include used/max MB readout", true));
        this.showBarSetting = addSetting(new BooleanSetting("showBar", "Progress Bar", "Show compact heap usage bar", true));
    }

    @Override
    protected void renderContent(DrawContext context, MinecraftClient client, boolean previewMode) {
        long now = System.currentTimeMillis();
        if (now - lastUpdateMs >= 300L || previewMode) {
            lastUpdateMs = now;
            Runtime rt = Runtime.getRuntime();
            long maxBytes = Math.max(1L, rt.maxMemory());
            long usedBytes = Math.max(0L, rt.totalMemory() - rt.freeMemory());
            long usedMb = usedBytes / (1024L * 1024L);
            long maxMb = maxBytes / (1024L * 1024L);
            cachedRatio = Math.max(0.0f, Math.min(1.0f, (float) usedBytes / (float) maxBytes));
            int pct = Math.round(cachedRatio * 100.0f);
            if (showMbSetting.get()) {
                cachedText = String.format(Locale.ROOT, "Mem: %d%% (%d/%d MB)", pct, usedMb, maxMb);
            } else {
                cachedText = "Mem: " + pct + "%";
            }
        }

        if (!showBarSetting.get()) {
            drawHudLinesBox(context, client.textRenderer, Collections.singletonList(cachedText));
            return;
        }

        TextRenderer font = client.textRenderer;
        int padX = 6;
        int boxW = Math.max(92, font.getWidth(cachedText) + padX * 2);
        int boxH = 20;
        setLastDimensions(boxW, boxH);

        if (backgroundSetting.get()) {
            int bgAlpha = bgOpacitySetting.getInt();
            context.fill(0, 0, boxW, boxH, ColorUtil.argb(bgAlpha, 11, 14, 19));
            context.drawStrokedRectangle(0, 0, boxW, boxH, ColorUtil.argb(Math.min(200, bgAlpha + 35), 34, 43, 56));
            if (accentBarSetting.get()) {
                context.fill(0, 0, 2, boxH, ColorUtil.FLUX_BLUE);
            }
        }

        context.drawText(font, cachedText, padX + 1, 3, textColorSetting.resolveArgb(255), shadowSetting.get());
        int barX = padX + 1;
        int barY = 14;
        int barW = boxW - padX * 2 - 1;
        context.fill(barX, barY, barX + barW, barY + 3, 0x991A222D);
        int fillCol = cachedRatio > 0.85f ? 0xFFE74C3C : (cachedRatio > 0.68f ? 0xFFF1C40F : ColorUtil.FLUX_BLUE);
        context.fill(barX, barY, barX + Math.round(barW * cachedRatio), barY + 3, fillCol);
    }
}
