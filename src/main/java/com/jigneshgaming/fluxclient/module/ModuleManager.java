package com.jigneshgaming.fluxclient.module;

import com.jigneshgaming.fluxclient.module.impl.hud.*;
import com.jigneshgaming.fluxclient.module.impl.movement.AutoSprintModule;
import com.jigneshgaming.fluxclient.module.impl.player.ToggleSneakModule;
import com.jigneshgaming.fluxclient.module.impl.render.*;
import com.jigneshgaming.fluxclient.module.impl.utility.AutoGgModule;
import com.jigneshgaming.fluxclient.module.impl.utility.NotificationsModule;
import com.jigneshgaming.fluxclient.module.impl.utility.QuickMessagesModule;
import com.jigneshgaming.fluxclient.module.impl.utility.ZoomModule;
import com.jigneshgaming.fluxclient.ui.HudEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.*;

public final class ModuleManager {
    private static ModuleManager instance;

    private final List<Module> modules = new ArrayList<>();
    private final List<HudModule> hudModules = new ArrayList<>();
    private final Map<Class<? extends Module>, Module> byClass = new HashMap<>();
    private final Map<String, Module> byId = new HashMap<>();

    // Direct O(1) typed module references for hot render & input hooks
    public final FpsModule fps;
    public final CoordinatesModule coordinates;
    public final DirectionModule direction;
    public final ClockModule clock;
    public final MemoryUsageModule memoryUsage;
    public final BiomeModule biome;
    public final ResourcePacksModule resourcePacks;
    public final ServerInfoModule serverInfo;

    public final FullBrightModule fullBright;
    public final BlockOverlayModule blockOverlay;
    public final HitboxesModule hitboxes;
    public final RemoveHurtShakeModule removeHurtShake;
    public final HideBossBarModule hideBossBar;
    public final TimeChangerModule timeChanger;
    public final CustomCrosshairModule customCrosshair;
    public final CustomScoreboardModule customScoreboard;

    public final AutoSprintModule autoSprint;
    public final ToggleSneakModule toggleSneak;

    public final ZoomModule zoom;
    public final AutoGgModule autoGg;
    public final QuickMessagesModule quickMessages;
    public final NotificationsModule notifications;

    public ModuleManager() {
        instance = this;

        // 1. HUD Modules (8) — exact screenshot order
        this.fps = register(new FpsModule());
        this.coordinates = register(new CoordinatesModule());
        this.direction = register(new DirectionModule());
        this.clock = register(new ClockModule());
        this.memoryUsage = register(new MemoryUsageModule());
        this.biome = register(new BiomeModule());
        this.resourcePacks = register(new ResourcePacksModule());
        this.serverInfo = register(new ServerInfoModule());

        // 2. Render Modules (8) — exact screenshot order
        this.fullBright = register(new FullBrightModule());
        this.blockOverlay = register(new BlockOverlayModule());
        this.hitboxes = register(new HitboxesModule());
        this.removeHurtShake = register(new RemoveHurtShakeModule());
        this.hideBossBar = register(new HideBossBarModule());
        this.timeChanger = register(new TimeChangerModule());
        this.customCrosshair = register(new CustomCrosshairModule());
        this.customScoreboard = register(new CustomScoreboardModule());

        // 3. Movement Modules (1)
        this.autoSprint = register(new AutoSprintModule());

        // 4. Player Modules (1)
        this.toggleSneak = register(new ToggleSneakModule());

        // 5. Utility Modules (4) — exact screenshot order
        this.zoom = register(new ZoomModule());
        this.autoGg = register(new AutoGgModule());
        this.quickMessages = register(new QuickMessagesModule());
        this.notifications = register(new NotificationsModule());
    }

    private <T extends Module> T register(T module) {
        modules.add(module);
        byClass.put(module.getClass(), module);
        byId.put(module.getId(), module);
        if (module instanceof HudModule hud) {
            hudModules.add(hud);
        }
        return module;
    }

    public static ModuleManager getInstance() {
        if (instance == null) {
            instance = new ModuleManager();
        }
        return instance;
    }

    public List<Module> getAllModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<HudModule> getHudModules() {
        return Collections.unmodifiableList(hudModules);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        return (T) byClass.get(clazz);
    }

    public Module getModuleById(String id) {
        return byId.get(id);
    }

    public List<Module> getFiltered(ModuleCategory category, String searchQuery) {
        return filterModules(category, searchQuery);
    }

    public List<Module> filterModules(ModuleCategory category, String searchQuery) {
        String q = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        List<Module> out = new ArrayList<>();
        for (Module m : modules) {
            if (category != null && category != ModuleCategory.ALL && m.getCategory() != category) {
                continue;
            }
            if (!q.isEmpty()) {
                boolean matchName = m.getName().toLowerCase(Locale.ROOT).contains(q);
                boolean matchDesc = m.getDescription().toLowerCase(Locale.ROOT).contains(q);
                boolean matchCat = m.getCategory().getDisplayName().toLowerCase(Locale.ROOT).contains(q);
                if (!matchName && !matchDesc && !matchCat) {
                    continue;
                }
            }
            out.add(m);
        }
        return out;
    }

    public void onTick(MinecraftClient client) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onTick(client);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public void renderHud(DrawContext context, MinecraftClient client) {
        if (client == null || client.options.hudHidden) {
            return;
        }
        if (client.currentScreen instanceof HudEditorScreen) {
            return;
        }
        if (client.getDebugHud() != null && client.getDebugHud().shouldShowDebugHud()) {
            return;
        }
        for (HudModule hud : hudModules) {
            if (hud.isEnabled()) {
                hud.renderHudElement(context, client, false);
            }
        }
    }
}
