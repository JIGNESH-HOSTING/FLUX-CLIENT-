package com.jigneshgaming.fluxclient.module.impl.hud;

import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public final class DirectionModule extends HudModule {
    private static final String[] CARDINALS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
    private static final String[] CARDINAL_WITH_AXIS = {
            "S [+Z]", "SW [-X / +Z]", "W [-X]", "NW [-X / -Z]",
            "N [-Z]", "NE [+X / -Z]", "E [+X]", "SE [+X / +Z]"
    };

    private final BooleanSetting showAxisSetting;
    private final BooleanSetting showDegreesSetting;

    private int lastDegInt = Integer.MIN_VALUE;
    private boolean lastAxis = true;
    private boolean lastShowDeg = false;
    private String cachedLine = "N [-Z]";

    public DirectionModule() {
        super(
                "direction",
                "Direction",
                "Displays cardinal compass direction, coordinate axis, and yaw angle.",
                ModuleIcon.DIRECTION,
                false,
                0.46f,
                0.018f
        );
        this.showAxisSetting = addSetting(new BooleanSetting("showAxis", "Show Axis", "Show coordinate axis (+X/-Z)", true));
        this.showDegreesSetting = addSetting(new BooleanSetting("showDegrees", "Show Degrees", "Show exact yaw angle", false));
    }

    @Override
    protected void renderContent(DrawContext context, MinecraftClient client, boolean previewMode) {
        Entity cam = client.getCameraEntity();
        if (cam == null) {
            if (previewMode) {
                drawHudLineBox(context, client.textRenderer, "N [-Z]");
            }
            return;
        }
        float yaw = MathHelper.wrapDegrees(cam.getYaw());
        int degInt = Math.round(yaw);
        boolean axis = showAxisSetting.get();
        boolean showDeg = showDegreesSetting.get();

        if (degInt != lastDegInt || axis != lastAxis || showDeg != lastShowDeg) {
            lastDegInt = degInt;
            lastAxis = axis;
            lastShowDeg = showDeg;
            int idx = Math.floorMod(Math.round(yaw / 45.0f), 8);
            String base = axis ? CARDINAL_WITH_AXIS[idx] : CARDINALS[idx];
            cachedLine = showDeg ? (base + " " + degInt + "°") : base;
        }
        drawHudLineBox(context, client.textRenderer, cachedLine);
    }
}
