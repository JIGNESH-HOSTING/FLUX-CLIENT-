package com.jigneshgaming.fluxclient.module.impl.hud;

import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public final class BiomeModule extends HudModule {
    private BlockPos lastBlockPos = null;
    private String cachedDisplay = "Biome: Plains";

    public BiomeModule() {
        super(
                "biome",
                "Biome",
                "Displays the current biome at your block coordinates.",
                ModuleIcon.BIOME,
                false,
                0.012f,
                0.205f
        );
    }

    @Override
    protected void renderContent(DrawContext context, MinecraftClient client, boolean previewMode) {
        Entity cam = client.getCameraEntity();
        if (cam == null || client.world == null) {
            if (previewMode) {
                drawHudLineBox(context, client.textRenderer, "Biome: Plains");
            }
            return;
        }
        BlockPos pos = cam.getBlockPos();
        if (!pos.equals(lastBlockPos)) {
            lastBlockPos = pos;
            Optional<RegistryKey<Biome>> keyOpt = client.world.getBiome(pos).getKey();
            if (keyOpt.isPresent()) {
                String raw = keyOpt.get().getValue().getPath();
                cachedDisplay = "Biome: " + formatBiomeName(raw);
            } else {
                cachedDisplay = "Biome: Unknown";
            }
        }
        drawHudLineBox(context, client.textRenderer, cachedDisplay);
    }

    private static String formatBiomeName(String path) {
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder(path.length() + 4);
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) {
                sb.append(p.substring(1));
            }
        }
        return sb.toString();
    }
}
