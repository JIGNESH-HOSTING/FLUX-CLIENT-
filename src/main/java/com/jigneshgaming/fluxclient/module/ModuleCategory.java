package com.jigneshgaming.fluxclient.module;

public enum ModuleCategory {
    ALL("All"),
    HUD("HUD"),
    RENDER("Render"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    UTILITY("Utility");

    private final String displayName;

    ModuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
