package com.jigneshgaming.fluxclient.module;

import com.jigneshgaming.fluxclient.config.FluxConfigManager;
import com.jigneshgaming.fluxclient.module.setting.Setting;
import com.jigneshgaming.fluxclient.ui.FluxNotificationOverlay;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final ModuleIcon icon;
    private final boolean defaultEnabled;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    protected Module(String id, String name, String description, ModuleCategory category, ModuleIcon icon, boolean defaultEnabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.icon = icon;
        this.defaultEnabled = defaultEnabled;
        this.enabled = defaultEnabled;
    }

    protected <S extends Setting<?>> S addSetting(S setting) {
        this.settings.add(setting);
        return setting;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public ModuleIcon getIcon() {
        return icon;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        setEnabled(enabled, true);
    }

    public void setEnabled(boolean newState, boolean notifyAndSave) {
        if (this.enabled == newState) {
            return;
        }
        this.enabled = newState;
        try {
            if (newState) {
                onEnable();
            } else {
                onDisable();
            }
        } catch (Throwable ignored) {
        }
        if (notifyAndSave) {
            FluxConfigManager.saveAsync();
            FluxNotificationOverlay.push(
                    this.name,
                    newState ? "Module enabled" : "Module disabled",
                    newState ? FluxNotificationOverlay.Type.SUCCESS : FluxNotificationOverlay.Type.INFO
            );
        }
    }

    public void toggle() {
        setEnabled(!this.enabled, true);
    }

    public void restoreDefaults() {
        for (Setting<?> s : settings) {
            s.reset();
        }
        setEnabled(this.defaultEnabled, false);
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public Setting<?> getSettingByKey(String key) {
        for (Setting<?> s : settings) {
            if (s.getKey().equals(key)) {
                return s;
            }
        }
        return null;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick(MinecraftClient client) {
    }
}
