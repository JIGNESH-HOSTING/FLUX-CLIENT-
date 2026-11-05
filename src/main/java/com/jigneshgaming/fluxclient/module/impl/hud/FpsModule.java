package com.jigneshgaming.fluxclient.module.impl.hud;

import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.Collections;
import java.util.Locale;

public final class FpsModule extends HudModule {
    private final ModeSetting formatSetting;
    private final BooleanSetting showFrameTimeSetting;

    private long lastUpdateMs = 0L;
    private String cachedLine = "60 FPS";

    public FpsModule() {
        super(
                "fps",
                "FPS",
                "Displays real-time client framerate without artificial inflation.",
                ModuleIcon.FPS,
                true,
                0.012f,
                0.018f
        );
        this.formatSetting = addSetting(new ModeSetting("format", "Format", "Label display format", "Value First", "Value First", "Bracketed", "Prefix"));
        this.showFrameTimeSetting = addSetting(new BooleanSetting("frameTime", "Frame Time (ms)", "Append frame render interval in ms", false));
    }

    @Override
    protected void renderContent(DrawContext context, MinecraftClient client, boolean previewMode) {
        long now = System.currentTimeMillis();
        if (now - lastUpdateMs >= 150L || previewMode) {
            lastUpdateMs = now;
            int fps = Math.max(0, client.getCurrentFps());
            String base = switch (formatSetting.getValue()) {
                case "Bracketed" -> "[" + fps + " FPS]";
                case "Prefix" -> "FPS: " + fps;
                default -> fps + " FPS";
            };
            if (showFrameTimeSetting.get() && fps > 0) {
                float ms = 1000.0f / fps;
                base = base + String.format(Locale.ROOT, " (%.1f ms)", ms);
            }
            cachedLine = base;
        }
        drawHudLinesBox(context, client.textRenderer, Collections.singletonList(cachedLine));
    }
}
