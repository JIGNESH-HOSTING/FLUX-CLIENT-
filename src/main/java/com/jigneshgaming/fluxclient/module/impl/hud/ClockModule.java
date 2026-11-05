package com.jigneshgaming.fluxclient.module.impl.hud;

import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ClockModule extends HudModule {
    private static final DateTimeFormatter FMT_12 = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);
    private static final DateTimeFormatter FMT_12_SEC = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US);
    private static final DateTimeFormatter FMT_24 = DateTimeFormatter.ofPattern("HH:mm", Locale.US);
    private static final DateTimeFormatter FMT_24_SEC = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US);

    private final ModeSetting modeSetting;
    private final BooleanSetting secondsSetting;

    private long lastUpdateMs = 0L;
    private String cachedTime = "12:00 PM";

    public ClockModule() {
        super(
                "clock",
                "Clock",
                "Displays system clock in 12h/24h format or in-game world time.",
                ModuleIcon.CLOCK,
                false,
                0.88f,
                0.018f
        );
        this.modeSetting = addSetting(new ModeSetting("mode", "Time Source", "Clock format or world day/time", "12-Hour", "12-Hour", "24-Hour", "World Time"));
        this.secondsSetting = addSetting(new BooleanSetting("seconds", "Show Seconds", "Include seconds on system clock", false));
    }

    @Override
    protected void renderContent(DrawContext context, MinecraftClient client, boolean previewMode) {
        long now = System.currentTimeMillis();
        if (now - lastUpdateMs >= 400L || previewMode) {
            lastUpdateMs = now;
            if (modeSetting.is("World Time") && client.world != null) {
                long dayTime = client.world.getTimeOfDay();
                long day = dayTime / 24000L;
                int ticksInDay = (int) (dayTime % 24000L);
                int hours = ((ticksInDay / 1000) + 6) % 24;
                int minutes = (int) ((ticksInDay % 1000) * 60L / 1000L);
                cachedTime = String.format(Locale.ROOT, "Day %d • %02d:%02d", day, hours, minutes);
            } else {
                LocalTime lt = LocalTime.now();
                if (modeSetting.is("24-Hour")) {
                    cachedTime = lt.format(secondsSetting.get() ? FMT_24_SEC : FMT_24);
                } else {
                    cachedTime = lt.format(secondsSetting.get() ? FMT_12_SEC : FMT_12);
                }
            }
        }
        drawHudLineBox(context, client.textRenderer, cachedTime);
    }
}
