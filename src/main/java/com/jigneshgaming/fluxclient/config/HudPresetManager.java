package com.jigneshgaming.fluxclient.config;

import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleManager;
import com.jigneshgaming.fluxclient.module.impl.hud.*;
import com.jigneshgaming.fluxclient.module.impl.movement.AutoSprintModule;
import com.jigneshgaming.fluxclient.module.impl.render.*;
import com.jigneshgaming.fluxclient.module.impl.utility.NotificationsModule;
import com.jigneshgaming.fluxclient.module.impl.utility.ZoomModule;
import com.jigneshgaming.fluxclient.optimization.LowEndOptimizer;
import com.jigneshgaming.fluxclient.ui.FluxNotificationOverlay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HudPresetManager {
    public record HudEntrySnapshot(boolean enabled, float xRatio, float yRatio, double scale) {
    }

    public record PresetInfo(String numberBadge, String id, String title, String description, int accentColor) {
        public String code() {
            return numberBadge;
        }

        public String name() {
            return title;
        }

        public String subtitle() {
            return description;
        }
    }

    public static final List<PresetInfo> PRESETS = List.of(
            new PresetInfo("01", "pvp_focus", "PvP Focus", "FPS, Server Info, Direction, Auto Sprint, Crosshair & Hurt Shake off", 0xFFF05A24),
            new PresetInfo("02", "stream_clean", "Stream Clean", "Readable essentials arranged away from center for clean gameplay", 0xFF2ECC71),
            new PresetInfo("03", "survival_scout", "Survival Scout", "Detailed navigation, Coordinates, Direction, Biome, Clock & Full Bright", 0xFF3498DB),
            new PresetInfo("04", "performance", "Performance", "FPS, Memory, Ping and low-noise rendering optimizations", 0xFFF1C40F),
            new PresetInfo("05", "builder_utility", "Builder Utility", "Coordinates, Direction, Clock, Block Overlay, Time Changer & Zoom", 0xFF9B59B6),
            new PresetInfo("06", "custom_layout", "Custom Layout", "Saved from your current personal HUD positions & active setup", 0xFF00D2D3)
    );

    private static final Map<String, Boolean> customModuleStates = new HashMap<>();
    private static final Map<String, HudEntrySnapshot> customHudLayout = new HashMap<>();
    private static boolean hasCustomSnapshot = false;

    private HudPresetManager() {
    }

    public static boolean hasCustomSnapshot() {
        return hasCustomSnapshot;
    }

    public static Map<String, Boolean> getCustomModuleStates() {
        return customModuleStates;
    }

    public static Map<String, HudEntrySnapshot> getCustomHudLayout() {
        return customHudLayout;
    }

    public static void loadCustomSnapshot(Map<String, Boolean> states, Map<String, HudEntrySnapshot> hudEntries) {
        customModuleStates.clear();
        customHudLayout.clear();
        if (states != null) {
            customModuleStates.putAll(states);
        }
        if (hudEntries != null) {
            customHudLayout.putAll(hudEntries);
        }
        hasCustomSnapshot = !customModuleStates.isEmpty() || !customHudLayout.isEmpty();
    }

    public static void saveCustomLayout(boolean showToast) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        customModuleStates.clear();
        customHudLayout.clear();
        for (Module m : mm.getAllModules()) {
            customModuleStates.put(m.getId(), m.isEnabled());
            if (m instanceof HudModule hud) {
                customHudLayout.put(hud.getId(), new HudEntrySnapshot(hud.isEnabled(), hud.getXRatio(), hud.getYRatio(), hud.getScale()));
            }
        }
        hasCustomSnapshot = true;
        FluxConfigManager.saveAsync();
        if (showToast) {
            FluxNotificationOverlay.push("Custom Layout Saved", "Saved active modules & HUD positions", FluxNotificationOverlay.Type.SUCCESS);
        }
    }

    public static void applyPreset(String presetId) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        FluxNotificationOverlay.setSuppressed(true);
        try {
            if ("custom_layout".equals(presetId)) {
                if (!hasCustomSnapshot) {
                    saveCustomLayout(false);
                } else {
                    for (Module m : mm.getAllModules()) {
                        Boolean st = customModuleStates.get(m.getId());
                        if (st != null) {
                            m.setEnabled(st, false);
                        }
                        if (m instanceof HudModule hud) {
                            HudEntrySnapshot snap = customHudLayout.get(hud.getId());
                            if (snap != null) {
                                hud.setPositionRatio(snap.xRatio(), snap.yRatio());
                                hud.setScale(snap.scale());
                                hud.setEnabled(snap.enabled(), false);
                            }
                        }
                    }
                }
            } else {
                // Disable HUD & render clutter first, keep Notifications on
                for (Module m : mm.getAllModules()) {
                    if (!(m instanceof NotificationsModule)) {
                        m.setEnabled(false, false);
                    }
                    if (m instanceof HudModule hud) {
                        hud.setScale(1.0);
                    }
                }

                switch (presetId) {
                    case "pvp_focus" -> {
                        enableHud(mm.getModule(FpsModule.class), 0.012f, 0.018f, 1.05);
                        enableHud(mm.getModule(ServerInfoModule.class), 0.012f, 0.064f, 1.0);
                        enableHud(mm.getModule(DirectionModule.class), 0.45f, 0.018f, 1.0);
                        enableHud(mm.getModule(ResourcePacksModule.class), 0.012f, 0.110f, 0.95);
                        mm.getModule(AutoSprintModule.class).setEnabled(true, false);
                        mm.getModule(RemoveHurtShakeModule.class).setEnabled(true, false);
                        mm.getModule(CustomCrosshairModule.class).setEnabled(true, false);
                        mm.getModule(CustomScoreboardModule.class).setEnabled(true, false);
                        mm.getModule(FullBrightModule.class).setEnabled(true, false);
                    }
                    case "stream_clean" -> {
                        enableHud(mm.getModule(FpsModule.class), 0.012f, 0.018f, 0.95);
                        enableHud(mm.getModule(CoordinatesModule.class), 0.012f, 0.060f, 0.95);
                        enableHud(mm.getModule(ClockModule.class), 0.89f, 0.018f, 0.95);
                        mm.getModule(AutoSprintModule.class).setEnabled(true, false);
                        mm.getModule(RemoveHurtShakeModule.class).setEnabled(true, false);
                    }
                    case "survival_scout" -> {
                        enableHud(mm.getModule(FpsModule.class), 0.012f, 0.018f, 1.0);
                        enableHud(mm.getModule(CoordinatesModule.class), 0.012f, 0.064f, 1.0);
                        enableHud(mm.getModule(BiomeModule.class), 0.012f, 0.165f, 1.0);
                        enableHud(mm.getModule(DirectionModule.class), 0.44f, 0.018f, 1.0);
                        enableHud(mm.getModule(ClockModule.class), 0.87f, 0.018f, 1.0);
                        mm.getModule(FullBrightModule.class).setEnabled(true, false);
                        mm.getModule(AutoSprintModule.class).setEnabled(true, false);
                        mm.getModule(ZoomModule.class).setEnabled(true, false);
                    }
                    case "performance" -> {
                        enableHud(mm.getModule(FpsModule.class), 0.012f, 0.018f, 1.0);
                        enableHud(mm.getModule(MemoryUsageModule.class), 0.012f, 0.064f, 1.0);
                        enableHud(mm.getModule(ServerInfoModule.class), 0.012f, 0.125f, 1.0);
                        mm.getModule(HideBossBarModule.class).setEnabled(true, false);
                        mm.getModule(RemoveHurtShakeModule.class).setEnabled(true, false);
                        mm.getModule(AutoSprintModule.class).setEnabled(true, false);
                        LowEndOptimizer opt = LowEndOptimizer.getInstance();
                        opt.setEntityCullingEnabled(true);
                        opt.setParticleLimiterEnabled(true);
                    }
                    case "builder_utility" -> {
                        enableHud(mm.getModule(CoordinatesModule.class), 0.012f, 0.018f, 1.05);
                        enableHud(mm.getModule(DirectionModule.class), 0.012f, 0.122f, 1.0);
                        enableHud(mm.getModule(BiomeModule.class), 0.012f, 0.168f, 1.0);
                        enableHud(mm.getModule(ClockModule.class), 0.87f, 0.018f, 1.0);
                        mm.getModule(BlockOverlayModule.class).setEnabled(true, false);
                        mm.getModule(TimeChangerModule.class).setEnabled(true, false);
                        mm.getModule(FullBrightModule.class).setEnabled(true, false);
                        mm.getModule(ZoomModule.class).setEnabled(true, false);
                    }
                }
            }
        } finally {
            FluxNotificationOverlay.setSuppressed(false);
        }

        FluxConfigManager.saveAsync();
        String title = PRESETS.stream()
                .filter(p -> p.id().equals(presetId))
                .map(PresetInfo::title)
                .findFirst()
                .orElse("Preset");
        FluxNotificationOverlay.push("HUD Preset Applied", title + " layout is now active", FluxNotificationOverlay.Type.SUCCESS);
    }

    private static void enableHud(HudModule hud, float x, float y, double scale) {
        if (hud == null) return;
        hud.setPositionRatio(x, y);
        hud.setScale(scale);
        hud.setEnabled(true, false);
    }

    public static void resetToDefaults() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;
        FluxNotificationOverlay.setSuppressed(true);
        try {
            for (Module m : mm.getAllModules()) {
                m.restoreDefaults();
            }
        } finally {
            FluxNotificationOverlay.setSuppressed(false);
        }
        FluxConfigManager.saveAsync();
        FluxNotificationOverlay.push("Defaults Restored", "Original FLUX HUD & module setup restored", FluxNotificationOverlay.Type.INFO);
    }

    public static void captureCurrentAsCustomLayout() {
        saveCustomLayout(false);
    }

    public static void disableAllModules() {
        removeAllModules();
        FluxNotificationOverlay.push("All Modules Disabled", "Disabled every FLUX module", FluxNotificationOverlay.Type.WARNING);
    }

    public static void removeAllModules() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;
        FluxNotificationOverlay.setSuppressed(true);
        try {
            for (Module m : mm.getAllModules()) {
                m.setEnabled(false, false);
            }
        } finally {
            FluxNotificationOverlay.setSuppressed(false);
        }
        FluxConfigManager.saveAsync();
    }
}
