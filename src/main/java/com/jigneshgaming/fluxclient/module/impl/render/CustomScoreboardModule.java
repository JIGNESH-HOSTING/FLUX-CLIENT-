package com.jigneshgaming.fluxclient.module.impl.render;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.scoreboard.number.NumberFormat;

import java.util.IdentityHashMap;
import java.util.Map;

public final class CustomScoreboardModule extends Module {
    private final Map<ScoreboardObjective, NumberFormat> reusableFormatMap = new IdentityHashMap<>(8);
    private final BooleanSetting hideRedNumbersSetting;
    private final BooleanSetting hideScoreboardSetting;
    private final NumberSetting scaleSetting;
    private final NumberSetting offsetYSetting;
    private final NumberSetting offsetXSetting;

    public CustomScoreboardModule() {
        super(
                "custom_scoreboard",
                "Custom Scoreboard",
                "Customizes sidebar scoreboard scale, position, red score numbers, or visibility.",
                ModuleCategory.RENDER,
                ModuleIcon.CUSTOM_SCOREBOARD,
                false
        );
        this.hideRedNumbersSetting = addSetting(new BooleanSetting("hideNumbers", "Hide Red Numbers", "Remove red score digits on the right", true));
        this.hideScoreboardSetting = addSetting(new BooleanSetting("hideAll", "Hide Scoreboard", "Completely hide the sidebar scoreboard", false));
        this.scaleSetting = addSetting(new NumberSetting("scale", "Scale", "Scoreboard size multiplier", 1.0, 0.65, 1.35, 0.05, "x"));
        this.offsetYSetting = addSetting(new NumberSetting("offsetY", "Vertical Offset", "Vertical screen offset in pixels", 0, -100, 100, 4, "px"));
        this.offsetXSetting = addSetting(new NumberSetting("offsetX", "Horizontal Offset", "Horizontal screen offset in pixels", 0, -80, 0, 2, "px"));
    }

    public void renderScoreboard(DrawContext context, RenderTickCounter tickCounter, HudElement vanillaRenderer) {
        if (!isEnabled()) {
            vanillaRenderer.render(context, tickCounter);
            return;
        }
        if (hideScoreboardSetting.get()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        boolean appliedBlankNumbers = false;

        if (hideRedNumbersSetting.get() && client.world != null) {
            Scoreboard scoreboard = client.world.getScoreboard();
            reusableFormatMap.clear();
            for (ScoreboardObjective obj : scoreboard.getObjectives()) {
                reusableFormatMap.put(obj, obj.getNumberFormat());
                obj.setNumberFormat(BlankNumberFormat.INSTANCE);
            }
            appliedBlankNumbers = true;
        }

        float scale = scaleSetting.getFloat();
        int ox = offsetXSetting.getInt();
        int oy = offsetYSetting.getInt();
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();

        context.getMatrices().pushMatrix();
        try {
            if (Math.abs(scale - 1.0f) > 0.001f) {
                context.getMatrices().translate(sw, sh * 0.5f);
                context.getMatrices().scale(scale, scale);
                context.getMatrices().translate(-sw, -sh * 0.5f);
            }
            if (ox != 0 || oy != 0) {
                context.getMatrices().translate(ox, oy);
            }
            vanillaRenderer.render(context, tickCounter);
        } finally {
            context.getMatrices().popMatrix();
            if (appliedBlankNumbers) {
                for (Map.Entry<ScoreboardObjective, NumberFormat> entry : reusableFormatMap.entrySet()) {
                    entry.getKey().setNumberFormat(entry.getValue());
                }
                reusableFormatMap.clear();
            }
        }
    }
}
