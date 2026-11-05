package com.jigneshgaming.fluxclient.config;

import com.google.gson.*;
import com.jigneshgaming.fluxclient.cosmetics.CosmeticsManager;
import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleManager;
import com.jigneshgaming.fluxclient.module.setting.*;
import com.jigneshgaming.fluxclient.optimization.LowEndOptimizer;
import com.jigneshgaming.fluxclient.ui.FluxNotificationOverlay;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FluxConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "FluxClient-ConfigWriter");
        t.setDaemon(true);
        return t;
    });

    private FluxConfigManager() {
    }

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("fluxclient").resolve("config.json");
    }

    public static void saveAsync() {
        JsonObject snapshot = buildJsonSnapshot();
        SAVE_EXECUTOR.submit(() -> writeAtomically(snapshot));
    }

    public static void saveSync() {
        writeAtomically(buildJsonSnapshot());
    }

    private static synchronized void writeAtomically(JsonObject root) {
        try {
            Path target = getConfigPath();
            Files.createDirectories(target.getParent());
            Path tmp = target.resolveSibling("config.json.tmp");
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException fallbackEx) {
            try {
                Path target = getConfigPath();
                Files.createDirectories(target.getParent());
                try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
                    GSON.toJson(root, writer);
                }
            } catch (IOException ignored) {
            }
        }
    }

    private static JsonObject buildJsonSnapshot() {
        JsonObject root = new JsonObject();
        root.addProperty("schema", 1);
        root.addProperty("client", "FLUX CLIENT 1.21.11");

        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null) {
            JsonObject modulesObj = new JsonObject();
            for (Module m : mm.getAllModules()) {
                JsonObject mObj = new JsonObject();
                mObj.addProperty("enabled", m.isEnabled());
                if (m instanceof HudModule hud) {
                    mObj.addProperty("xRatio", hud.getXRatio());
                    mObj.addProperty("yRatio", hud.getYRatio());
                }
                JsonObject settingsObj = new JsonObject();
                for (Setting<?> s : m.getSettings()) {
                    Object val = s.getValue();
                    if (val instanceof Boolean b) {
                        settingsObj.addProperty(s.getKey(), b);
                    } else if (val instanceof Number n) {
                        settingsObj.addProperty(s.getKey(), n);
                    } else if (val != null) {
                        settingsObj.addProperty(s.getKey(), val.toString());
                    }
                }
                mObj.add("settings", settingsObj);
                modulesObj.add(m.getId(), mObj);
            }
            root.add("modules", modulesObj);
        }

        // Custom layout snapshot
        if (HudPresetManager.hasCustomSnapshot()) {
            JsonObject customObj = new JsonObject();
            JsonObject statesObj = new JsonObject();
            for (Map.Entry<String, Boolean> e : HudPresetManager.getCustomModuleStates().entrySet()) {
                statesObj.addProperty(e.getKey(), e.getValue());
            }
            customObj.add("states", statesObj);

            JsonObject hudObj = new JsonObject();
            for (Map.Entry<String, HudPresetManager.HudEntrySnapshot> e : HudPresetManager.getCustomHudLayout().entrySet()) {
                JsonObject snap = new JsonObject();
                snap.addProperty("enabled", e.getValue().enabled());
                snap.addProperty("xRatio", e.getValue().xRatio());
                snap.addProperty("yRatio", e.getValue().yRatio());
                snap.addProperty("scale", e.getValue().scale());
                hudObj.add(e.getKey(), snap);
            }
            customObj.add("hud", hudObj);
            root.add("customPreset", customObj);
        }

        // Cosmetics
        root.addProperty("activeCape", CosmeticsManager.getInstance().getActiveCape().name());

        // Low-End Optimizer
        LowEndOptimizer opt = LowEndOptimizer.getInstance();
        JsonObject optObj = new JsonObject();
        optObj.addProperty("entityCulling", opt.isEntityCullingEnabled());
        optObj.addProperty("passiveEntityMaxDistance", opt.getPassiveEntityMaxDistance());
        optObj.addProperty("itemEntityMaxDistance", opt.getItemEntityMaxDistance());
        optObj.addProperty("blockEntityCulling", opt.isBlockEntityCullingEnabled());
        optObj.addProperty("blockEntityMaxDistance", opt.getBlockEntityMaxDistance());
        optObj.addProperty("particleLimiter", opt.isParticleLimiterEnabled());
        optObj.addProperty("maxParticlesPerTick", opt.getMaxParticlesPerTick());
        optObj.addProperty("particleMaxDistance", opt.getParticleMaxDistance());
        optObj.addProperty("menuBlurBypass", opt.isMenuBlurBypass());
        optObj.addProperty("unfocusedFpsLimiter", opt.isUnfocusedFpsLimiter());
        optObj.addProperty("unfocusedMaxFps", opt.getUnfocusedMaxFps());
        root.add("optimization", optObj);

        return root;
    }

    public static void load() {
        Path path = getConfigPath();
        if (!Files.exists(path)) {
            saveAsync();
            return;
        }
        FluxNotificationOverlay.setSuppressed(true);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                return;
            }
            JsonObject root = parsed.getAsJsonObject();
            ModuleManager mm = ModuleManager.getInstance();

            if (mm != null && root.has("modules") && root.get("modules").isJsonObject()) {
                JsonObject modulesObj = root.getAsJsonObject("modules");
                for (Module m : mm.getAllModules()) {
                    if (!modulesObj.has(m.getId()) || !modulesObj.get(m.getId()).isJsonObject()) {
                        continue;
                    }
                    JsonObject mObj = modulesObj.getAsJsonObject(m.getId());
                    if (mObj.has("enabled")) {
                        m.setEnabled(mObj.get("enabled").getAsBoolean(), false);
                    }
                    if (m instanceof HudModule hud && mObj.has("xRatio") && mObj.has("yRatio")) {
                        hud.setPositionRatio(mObj.get("xRatio").getAsFloat(), mObj.get("yRatio").getAsFloat());
                    }
                    if (mObj.has("settings") && mObj.get("settings").isJsonObject()) {
                        JsonObject sObj = mObj.getAsJsonObject("settings");
                        for (Setting<?> setting : m.getSettings()) {
                            if (!sObj.has(setting.getKey())) continue;
                            JsonElement el = sObj.get(setting.getKey());
                            applyJsonToSetting(setting, el);
                        }
                    }
                }
            }

            if (root.has("customPreset") && root.get("customPreset").isJsonObject()) {
                JsonObject customObj = root.getAsJsonObject("customPreset");
                Map<String, Boolean> states = new HashMap<>();
                Map<String, HudPresetManager.HudEntrySnapshot> hudMap = new HashMap<>();
                if (customObj.has("states") && customObj.get("states").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> e : customObj.getAsJsonObject("states").entrySet()) {
                        states.put(e.getKey(), e.getValue().getAsBoolean());
                    }
                }
                if (customObj.has("hud") && customObj.get("hud").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> e : customObj.getAsJsonObject("hud").entrySet()) {
                        if (e.getValue().isJsonObject()) {
                            JsonObject snap = e.getValue().getAsJsonObject();
                            hudMap.put(e.getKey(), new HudPresetManager.HudEntrySnapshot(
                                    snap.get("enabled").getAsBoolean(),
                                    snap.get("xRatio").getAsFloat(),
                                    snap.get("yRatio").getAsFloat(),
                                    snap.get("scale").getAsDouble()
                            ));
                        }
                    }
                }
                HudPresetManager.loadCustomSnapshot(states, hudMap);
            }

            if (root.has("activeCape")) {
                try {
                    CosmeticsManager.CapeStyle style = CosmeticsManager.CapeStyle.valueOf(root.get("activeCape").getAsString());
                    CosmeticsManager.getInstance().setActiveCape(style, false);
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (root.has("optimization") && root.get("optimization").isJsonObject()) {
                JsonObject optObj = root.getAsJsonObject("optimization");
                LowEndOptimizer opt = LowEndOptimizer.getInstance();
                if (optObj.has("entityCulling")) opt.setEntityCullingEnabled(optObj.get("entityCulling").getAsBoolean());
                if (optObj.has("passiveEntityMaxDistance")) opt.setPassiveEntityMaxDistance(optObj.get("passiveEntityMaxDistance").getAsInt());
                if (optObj.has("itemEntityMaxDistance")) opt.setItemEntityMaxDistance(optObj.get("itemEntityMaxDistance").getAsInt());
                if (optObj.has("blockEntityCulling")) opt.setBlockEntityCullingEnabled(optObj.get("blockEntityCulling").getAsBoolean());
                if (optObj.has("blockEntityMaxDistance")) opt.setBlockEntityMaxDistance(optObj.get("blockEntityMaxDistance").getAsInt());
                if (optObj.has("particleLimiter")) opt.setParticleLimiterEnabled(optObj.get("particleLimiter").getAsBoolean());
                if (optObj.has("maxParticlesPerTick")) opt.setMaxParticlesPerTick(optObj.get("maxParticlesPerTick").getAsInt());
                if (optObj.has("particleMaxDistance")) opt.setParticleMaxDistance(optObj.get("particleMaxDistance").getAsInt());
                if (optObj.has("menuBlurBypass")) opt.setMenuBlurBypass(optObj.get("menuBlurBypass").getAsBoolean());
                if (optObj.has("unfocusedFpsLimiter")) opt.setUnfocusedFpsLimiter(optObj.get("unfocusedFpsLimiter").getAsBoolean());
                if (optObj.has("unfocusedMaxFps")) opt.setUnfocusedMaxFps(optObj.get("unfocusedMaxFps").getAsInt());
            }
        } catch (Throwable ignored) {
        } finally {
            FluxNotificationOverlay.setSuppressed(false);
        }
    }

    private static void applyJsonToSetting(Setting<?> setting, JsonElement el) {
        try {
            if (setting instanceof BooleanSetting bs) {
                bs.setValue(el.getAsBoolean());
            } else if (setting instanceof NumberSetting ns) {
                ns.setValue(el.getAsDouble());
            } else if (setting instanceof ModeSetting ms) {
                ms.setValue(el.getAsString());
            } else if (setting instanceof ColorSetting cs) {
                cs.setValue(el.getAsString());
            } else if (setting instanceof StringSetting ss) {
                ss.setValue(el.getAsString());
            }
        } catch (Throwable ignored) {
        }
    }
}
