package com.jigneshgaming.fluxclient.module.impl.hud;

import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.Locale;

public final class DirectionModule extends HudModule {
    private static final String[] CARDINALS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
    private static final String[] AXES = {"+Z", "-X / +Z", "-X", "-X / -Z", "-Z", "+X / -Z", "+X", "+X / +Z"};

    private final BooleanSetting showAxisSetting;
    private final BooleanSetting showDegreesSetting;

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
                drawHudLinesBox(context, client.textRenderer, Collections.singletonList("Facing: N (-Z)"));
            }
            return;
        }
        float yaw = MathHelper.wrapDegrees(cam.getYaw());
        int idx = Math.floorMod(Math.round(yaw / 45.0f), 8);
        StringBuilder sb = new StringBuilder(24);
        sb.append(CARDINALS[idx]);
        if (showAxisSetting.get()) {
            sb.append(" [").append(AXES[idx]).append(']');
        }
        if (showDegreesSetting.get()) {
            sb.append(String.format(Locale.ROOT, " %.0f°", yaw));
        }
        drawHudLinesBox(context, client.textRenderer, Collections.singletonList(sb.toString()));
    }
}
