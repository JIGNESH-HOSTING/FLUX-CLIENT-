package com.jigneshgaming.fluxclient.module.impl.hud;

import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class CoordinatesModule extends HudModule {
    private final ModeSetting layoutSetting;
    private final BooleanSetting decimalsSetting;
    private final BooleanSetting netherCoordsSetting;

    private long lastUpdateMs = 0L;
    private final List<String> cachedLines = new ArrayList<>(4);

    public CoordinatesModule() {
        super(
                "coordinates",
                "Coordinates",
                "Displays live X, Y, Z player coordinates and optional Nether/Overworld conversion.",
                ModuleIcon.COORDINATES,
                false,
                0.012f,
                0.068f
        );
        this.layoutSetting = addSetting(new ModeSetting("layout", "Layout", "Multi-line or single-line layout", "Multi-Line", "Multi-Line", "Compact"));
        this.decimalsSetting = addSetting(new BooleanSetting("decimals", "Show Decimals", "Show 1 decimal place for precision", false));
        this.netherCoordsSetting = addSetting(new BooleanSetting("netherLink", "Dimension Convert", "Show corresponding Nether/Overworld X/Z", false));
    }

    @Override
    protected void renderContent(DrawContext context, MinecraftClient client, boolean previewMode) {
        Entity cam = client.getCameraEntity();
        if (cam == null) {
            if (previewMode) {
                drawHudLinesBox(context, client.textRenderer, List.of("X: 128", "Y: 64", "Z: -256"));
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastUpdateMs >= 80L) {
            lastUpdateMs = now;
            cachedLines.clear();
            double x = cam.getX();
            double y = cam.getY();
            double z = cam.getZ();

            if (layoutSetting.is("Compact")) {
                if (decimalsSetting.get()) {
                    cachedLines.add(String.format(Locale.ROOT, "XYZ: %.1f, %.1f, %.1f", x, y, z));
                } else {
                    cachedLines.add(String.format(Locale.ROOT, "XYZ: %d, %d, %d", (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)));
                }
            } else {
                if (decimalsSetting.get()) {
                    cachedLines.add(String.format(Locale.ROOT, "X: %.1f", x));
                    cachedLines.add(String.format(Locale.ROOT, "Y: %.1f", y));
                    cachedLines.add(String.format(Locale.ROOT, "Z: %.1f", z));
                } else {
                    cachedLines.add("X: " + (int) Math.floor(x));
                    cachedLines.add("Y: " + (int) Math.floor(y));
                    cachedLines.add("Z: " + (int) Math.floor(z));
                }
            }

            if (netherCoordsSetting.get() && client.world != null) {
                boolean inNether = client.world.getRegistryKey() == World.NETHER;
                boolean inOverworld = client.world.getRegistryKey() == World.OVERWORLD;
                if (inNether) {
                    cachedLines.add(String.format(Locale.ROOT, "OW: %d, %d", (int) Math.floor(x * 8.0), (int) Math.floor(z * 8.0)));
                } else if (inOverworld) {
                    cachedLines.add(String.format(Locale.ROOT, "Nether: %d, %d", (int) Math.floor(x / 8.0), (int) Math.floor(z / 8.0)));
                }
            }
        }

        drawHudLinesBox(context, client.textRenderer, cachedLines.isEmpty() ? Collections.singletonList("XYZ: 0, 64, 0") : cachedLines);
    }
}
