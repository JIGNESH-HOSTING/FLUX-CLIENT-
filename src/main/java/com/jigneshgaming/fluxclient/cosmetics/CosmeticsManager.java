package com.jigneshgaming.fluxclient.cosmetics;

import com.jigneshgaming.fluxclient.config.FluxConfigManager;
import com.jigneshgaming.fluxclient.ui.FluxNotificationOverlay;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;

public final class CosmeticsManager {
    public enum CapeStyle {
        NONE("None (Vanilla)", "Uses your account's default cape", null, 0xFF546070),
        FLUX_BLUE("Flux Electric Blue", "Signature obsidian & electric blue FLUX cape", Identifier.of("fluxclient", "capes/flux_blue"), 0xFF2EA8FF),
        FLUX_ORANGE("Flux Inferno Orange", "Mod Menu orange & carbon black edition", Identifier.of("fluxclient", "capes/flux_orange"), 0xFFEE5A24),
        FLUX_STEALTH("Flux Stealth Carbon", "Minimalist matte carbon & silver trim", Identifier.of("fluxclient", "capes/flux_stealth"), 0xFFB8C4D4),
        FLUX_GOLD("Flux Cyber Gold", "Prestige royal dark & gold crest cape", Identifier.of("fluxclient", "capes/flux_gold"), 0xFFF1C40F);

        private final String displayName;
        private final String description;
        private final AssetInfo.TextureAssetInfo assetInfo;
        private final Identifier guiTexturePath;
        private final int previewColor;

        CapeStyle(String displayName, String description, Identifier textureId, int previewColor) {
            this.displayName = displayName;
            this.description = description;
            this.assetInfo = textureId == null ? null : new AssetInfo.TextureAssetInfo(textureId);
            this.guiTexturePath = textureId == null ? null : Identifier.of(textureId.getNamespace(), "textures/" + textureId.getPath() + ".png");
            this.previewColor = previewColor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public AssetInfo.TextureAssetInfo getAssetInfo() {
            return assetInfo;
        }

        public Identifier getGuiTexturePath() {
            return guiTexturePath;
        }

        public int getPreviewColor() {
            return previewColor;
        }
    }

    private static final CosmeticsManager INSTANCE = new CosmeticsManager();

    private CapeStyle activeCape = CapeStyle.FLUX_BLUE;

    private CosmeticsManager() {
    }

    public static CosmeticsManager getInstance() {
        return INSTANCE;
    }

    public CapeStyle getActiveCape() {
        return activeCape;
    }

    public void setActiveCape(CapeStyle style, boolean notifyAndSave) {
        if (style == null) {
            style = CapeStyle.NONE;
        }
        this.activeCape = style;
        if (notifyAndSave) {
            FluxConfigManager.saveAsync();
            FluxNotificationOverlay.push("Cosmetics Updated", "Equipped: " + style.getDisplayName(), FluxNotificationOverlay.Type.INFO);
        }
    }
}
