package com.jigneshgaming.fluxclient.module.impl.utility;

import com.jigneshgaming.fluxclient.FluxClientMod;
import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.ModeSetting;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;

public final class ZoomModule extends Module {
    private final ModeSetting activationSetting;
    private final NumberSetting zoomLevelSetting;
    private final BooleanSetting smoothZoomSetting;
    private final BooleanSetting scrollZoomSetting;
    private final BooleanSetting scaleSensitivitySetting;

    private float currentFactor = 1.0f;
    private double scrollOffset = 0.0;
    private boolean keyToggleState = false;
    private boolean wasZoomKeyDown = false;

    public ZoomModule() {
        super(
                "zoom",
                "Zoom",
                "Smooth optical FOV zoom with scroll magnification and sensitivity scaling.",
                ModuleCategory.UTILITY,
                ModuleIcon.ZOOM,
                false
        );
        this.activationSetting = addSetting(new ModeSetting(
                "activation",
                "Activation",
                "Hold C key, toggle C key, or zoom whenever module is enabled",
                "Hold Key",
                "Hold Key",
                "Toggle Key",
                "While Enabled"
        ));
        this.zoomLevelSetting = addSetting(new NumberSetting("zoomLevel", "Zoom Level", "Magnification factor", 4.0, 2.0, 12.0, 0.5, "x"));
        this.smoothZoomSetting = addSetting(new BooleanSetting("smooth", "Smooth Transition", "Interpolate FOV smoothly", true));
        this.scrollZoomSetting = addSetting(new BooleanSetting("scrollAdjust", "Scroll To Zoom", "Use mouse wheel while zooming to adjust magnification", true));
        this.scaleSensitivitySetting = addSetting(new BooleanSetting("scaleSensitivity", "Smooth Camera", "Scale down look sensitivity proportionally while zoomed", true));
    }

    @Override
    protected void onDisable() {
        scrollOffset = 0.0;
        keyToggleState = false;
        wasZoomKeyDown = false;
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || FluxClientMod.ZOOM_KEY == null) {
            keyToggleState = false;
            wasZoomKeyDown = false;
            scrollOffset = 0.0;
            return;
        }
        boolean keyDown = FluxClientMod.ZOOM_KEY.isPressed() && client.currentScreen == null;
        if (activationSetting.is("Toggle Key")) {
            if (keyDown && !wasZoomKeyDown) {
                keyToggleState = !keyToggleState;
                if (!keyToggleState) {
                    scrollOffset = 0.0;
                }
            }
        } else if (!keyDown && activationSetting.is("Hold Key")) {
            scrollOffset = 0.0;
        }
        wasZoomKeyDown = keyDown;
    }

    public boolean isActivelyZooming() {
        if (!isEnabled()) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.currentScreen != null) {
            return false;
        }
        return switch (activationSetting.getValue()) {
            case "While Enabled" -> true;
            case "Toggle Key" -> keyToggleState;
            default -> FluxClientMod.ZOOM_KEY != null && FluxClientMod.ZOOM_KEY.isPressed();
        };
    }

    public boolean onScroll(double vertical) {
        if (!isActivelyZooming() || !scrollZoomSetting.get() || vertical == 0.0) {
            return false;
        }
        scrollOffset = Math.max(-2.5, Math.min(14.0, scrollOffset + (vertical > 0 ? 0.75 : -0.75)));
        return true;
    }

    public float applyFov(float baseFov) {
        boolean zooming = isActivelyZooming();
        double effectiveMag = Math.max(1.25, zoomLevelSetting.get() + scrollOffset);
        float target = zooming ? (float) (1.0 / effectiveMag) : 1.0f;

        if (smoothZoomSetting.get()) {
            currentFactor += (target - currentFactor) * 0.26f;
            if (Math.abs(target - currentFactor) < 0.002f) {
                currentFactor = target;
            }
        } else {
            currentFactor = target;
        }

        if (Math.abs(currentFactor - 1.0f) < 0.002f) {
            return baseFov;
        }
        return Math.max(5.0f, Math.min(150.0f, baseFov * currentFactor));
    }

    public double getSensitivityMultiplier() {
        if (!scaleSensitivitySetting.get() || Math.abs(currentFactor - 1.0f) < 0.01f) {
            return 1.0;
        }
        return Math.max(0.12, currentFactor);
    }
}
