package com.jigneshgaming.fluxclient.module.impl.hud;

import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.ResourcePackProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ResourcePacksModule extends HudModule {
    private final BooleanSetting hideInternalSetting;
    private long lastUpdateMs = 0L;
    private String cachedLine = "Packs: Vanilla";

    public ResourcePacksModule() {
        super(
                "resource_packs",
                "Resource Packs",
                "Displays active user resource packs and enabled count.",
                ModuleIcon.RESOURCE_PACKS,
                false,
                0.012f,
                0.245f
        );
        this.hideInternalSetting = addSetting(new BooleanSetting("hideInternal", "Hide Built-In", "Filter out internal vanilla/fabric packs", true));
    }

    @Override
    protected void renderContent(DrawContext context, MinecraftClient client, boolean previewMode) {
        long now = System.currentTimeMillis();
        if (now - lastUpdateMs >= 1000L || previewMode) {
            lastUpdateMs = now;
            if (client.getResourcePackManager() != null) {
                Collection<ResourcePackProfile> enabled = client.getResourcePackManager().getEnabledProfiles();
                List<String> userPacks = new ArrayList<>();
                for (ResourcePackProfile profile : enabled) {
                    String id = profile.getId();
                    if (hideInternalSetting.get() && isBuiltInPack(id)) {
                        continue;
                    }
                    String cleanName = profile.getDisplayName().getString();
                    if (cleanName.startsWith("file/")) {
                        cleanName = cleanName.substring(5);
                    }
                    userPacks.add(cleanName);
                }
                if (userPacks.isEmpty()) {
                    cachedLine = "Pack: Default";
                } else if (userPacks.size() == 1) {
                    cachedLine = "Pack: " + userPacks.get(0);
                } else {
                    cachedLine = "Pack: " + userPacks.get(userPacks.size() - 1) + " (+" + (userPacks.size() - 1) + ")";
                }
            }
        }
        drawHudLinesBox(context, client.textRenderer, Collections.singletonList(cachedLine));
    }

    private static boolean isBuiltInPack(String id) {
        if (id == null) return true;
        String lower = id.toLowerCase();
        return lower.equals("vanilla")
                || lower.equals("fabric")
                || lower.equals("programmer_art")
                || lower.equals("high_contrast")
                || lower.startsWith("fabric-")
                || lower.equals("fluxclient");
    }
}
