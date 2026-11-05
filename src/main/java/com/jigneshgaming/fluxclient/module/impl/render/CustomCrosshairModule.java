package com.jigneshgaming.fluxclient.module.impl.render;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.ColorSetting;
import com.jigneshgaming.fluxclient.module.setting.ModeSetting;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public final class CustomCrosshairModule extends Module {
    private final ModeSetting styleSetting;
    private final NumberSetting sizeSetting;
    private final NumberSetting gapSetting;
    private final NumberSetting thicknessSetting;
    private final BooleanSetting centerDotSetting;
    private final BooleanSetting outlineSetting;
    private final BooleanSetting dynamicTargetSetting;
    private final ColorSetting colorSetting;

    public CustomCrosshairModule() {
        super(
                "custom_crosshair",
                "Custom Crosshair",
                "Replaces the vanilla crosshair with crisp PvP styles and target highlight.",
                ModuleCategory.RENDER,
                ModuleIcon.CUSTOM_CROSSHAIR,
                false
        );
        this.styleSetting = addSetting(new ModeSetting("style", "Style", "Crosshair shape", "Cross", "Cross", "Dot", "Square Ring", "Chevron"));
        this.sizeSetting = addSetting(new NumberSetting("size", "Arm Length", "Length of crosshair arms", 4, 2, 12, 1, "px"));
        this.gapSetting = addSetting(new NumberSetting("gap", "Center Gap", "Gap from screen center", 2, 0, 8, 1, "px"));
        this.thicknessSetting = addSetting(new NumberSetting("thickness", "Thickness", "Crosshair stroke thickness", 1, 1, 3, 1, "px"));
        this.centerDotSetting = addSetting(new BooleanSetting("centerDot", "Center Dot", "Draw a 1px/2px dot in center", false));
        this.outlineSetting = addSetting(new BooleanSetting("outline", "Dark Outline", "Draw high-contrast 1px black border", true));
        this.dynamicTargetSetting = addSetting(new BooleanSetting("targetHighlight", "Target Red Highlight", "Turn red when aiming at a living entity", true));
        this.colorSetting = addSetting(new ColorSetting("color", "Color", "Crosshair color", "White"));
    }

    public void renderCrosshair(DrawContext context, MinecraftClient client) {
        if (!client.options.getPerspective().isFirstPerson()) {
            return;
        }
        int cx = context.getScaledWindowWidth() / 2;
        int cy = context.getScaledWindowHeight() / 2;

        int color = colorSetting.resolveArgb(255);
        if (dynamicTargetSetting.get() && client.crosshairTarget instanceof EntityHitResult ehr && ehr.getType() == HitResult.Type.ENTITY) {
            if (ehr.getEntity() instanceof LivingEntity) {
                color = 0xFFFF3B30;
            }
        }

        int size = sizeSetting.getInt();
        int gap = gapSetting.getInt();
        int thick = thicknessSetting.getInt();
        boolean outline = outlineSetting.get();
        int halfThick = thick / 2;

        switch (styleSetting.getValue()) {
            case "Dot" -> drawRectWithOutline(context, cx - halfThick - 1, cy - halfThick - 1, cx + halfThick + 1, cy + halfThick + 1, color, outline);
            case "Square Ring" -> {
                int r = gap + size;
                drawRectWithOutline(context, cx - r, cy - r, cx + r + 1, cy - r + thick, color, outline);
                drawRectWithOutline(context, cx - r, cy + r - thick + 1, cx + r + 1, cy + r + 1, color, outline);
                drawRectWithOutline(context, cx - r, cy - r, cx - r + thick, cy + r + 1, color, outline);
                drawRectWithOutline(context, cx + r - thick + 1, cy - r, cx + r + 1, cy + r + 1, color, outline);
            }
            case "Chevron" -> {
                for (int i = 0; i <= size; i++) {
                    drawRectWithOutline(context, cx - i, cy + gap + i, cx - i + thick, cy + gap + i + thick, color, outline);
                    drawRectWithOutline(context, cx + i, cy + gap + i, cx + i + thick, cy + gap + i + thick, color, outline);
                }
            }
            default -> {
                // Top, Bottom, Left, Right arms
                drawRectWithOutline(context, cx - halfThick, cy - gap - size, cx - halfThick + thick, cy - gap, color, outline);
                drawRectWithOutline(context, cx - halfThick, cy + gap + 1, cx - halfThick + thick, cy + gap + 1 + size, color, outline);
                drawRectWithOutline(context, cx - gap - size, cy - halfThick, cx - gap, cy - halfThick + thick, color, outline);
                drawRectWithOutline(context, cx + gap + 1, cy - halfThick, cx + gap + 1 + size, cy - halfThick + thick, color, outline);
            }
        }

        if (centerDotSetting.get() && !styleSetting.is("Dot")) {
            drawRectWithOutline(context, cx - halfThick, cy - halfThick, cx - halfThick + thick, cy - halfThick + thick, color, outline);
        }
    }

    private static void drawRectWithOutline(DrawContext context, int x1, int y1, int x2, int y2, int color, boolean outline) {
        if (outline) {
            context.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xCC000000);
        }
        context.fill(x1, y1, x2, y2, color);
    }
}
