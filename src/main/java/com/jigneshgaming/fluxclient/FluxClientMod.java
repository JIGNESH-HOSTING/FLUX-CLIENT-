package com.jigneshgaming.fluxclient;

import com.jigneshgaming.fluxclient.config.FluxConfigManager;
import com.jigneshgaming.fluxclient.module.ModuleManager;
import com.jigneshgaming.fluxclient.module.impl.render.CustomCrosshairModule;
import com.jigneshgaming.fluxclient.module.impl.render.CustomScoreboardModule;
import com.jigneshgaming.fluxclient.optimization.LowEndOptimizer;
import com.jigneshgaming.fluxclient.ui.FluxModMenuScreen;
import com.jigneshgaming.fluxclient.ui.FluxNotificationOverlay;
import com.jigneshgaming.fluxclient.ui.HudEditorScreen;
import com.jigneshgaming.fluxclient.ui.QuickMessagesScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Fabric client entrypoint for FLUX CLIENT 1.21.11.
 * Developed by JIGNESHGAMING-k7j.
 */
public final class FluxClientMod implements ClientModInitializer {
    public static final String MOD_ID = "fluxclient";
    public static final String MOD_NAME = "FLUX CLIENT";
    public static final String MOD_VERSION = "1.0.0+1.21.11";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static KeyBinding OPEN_MENU_KEY;
    public static KeyBinding HUD_EDITOR_KEY;
    public static KeyBinding ZOOM_KEY;
    public static KeyBinding QUICK_MESSAGES_KEY;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {} ({}) by JIGNESHGAMING-k7j...", MOD_NAME, MOD_VERSION);

        // 1. Initialize all 22 modules & load persisted JSON config
        ModuleManager moduleManager = ModuleManager.getInstance();
        FluxConfigManager.load();

        // 2. Register Keybindings
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "general"));
        OPEN_MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fluxclient.open_mod_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));
        HUD_EDITOR_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fluxclient.hud_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                category
        ));
        ZOOM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fluxclient.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                category
        ));
        QUICK_MESSAGES_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fluxclient.quick_messages",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                category
        ));

        // 3. Register Client Tick Loop
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LowEndOptimizer.getInstance().onClientTick(client);
            moduleManager.onTick(client);

            while (OPEN_MENU_KEY.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new FluxModMenuScreen(null));
                }
            }
            while (HUD_EDITOR_KEY.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new HudEditorScreen(null));
                }
            }
            while (QUICK_MESSAGES_KEY.wasPressed()) {
                if (client.currentScreen == null && moduleManager.quickMessages.isEnabled()) {
                    client.setScreen(new QuickMessagesScreen(null));
                }
            }
        });

        // 4. Register HUD Overlay & Vanilla HUD Replacements
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "hud_overlay"),
                (context, tickCounter) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    moduleManager.renderHud(context, client);
                    FluxNotificationOverlay.render(context, client);
                }
        );

        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, vanillaCrosshair -> (context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            CustomCrosshairModule crosshair = moduleManager.customCrosshair;
            if (crosshair.isEnabled() && client != null && !client.options.hudHidden) {
                if (client.options.getPerspective().isFirstPerson()) {
                    crosshair.renderCrosshair(context, client);
                }
            } else {
                vanillaCrosshair.render(context, tickCounter);
            }
        });

        HudElementRegistry.replaceElement(VanillaHudElements.BOSS_BAR, vanillaBossBar -> (context, tickCounter) -> {
            if (!moduleManager.hideBossBar.isEnabled()) {
                vanillaBossBar.render(context, tickCounter);
            }
        });

        HudElementRegistry.replaceElement(VanillaHudElements.SCOREBOARD, vanillaScoreboard -> (context, tickCounter) -> {
            CustomScoreboardModule sb = moduleManager.customScoreboard;
            if (sb.isEnabled()) {
                sb.renderScoreboard(context, tickCounter, vanillaScoreboard);
            } else {
                vanillaScoreboard.render(context, tickCounter);
            }
        });

        // 5. Register 3D World Render Hooks (Block Overlay & Entity Hitboxes)
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineRenderState) ->
                !moduleManager.blockOverlay.renderBlockOutline(context, outlineRenderState)
        );

        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(moduleManager.hitboxes::renderHitboxes);

        // 6. Register Chat/Game Message Hooks (Auto GG)
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                moduleManager.autoGg.onIncomingMessage(message);
            }
        });
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
                moduleManager.autoGg.onIncomingMessage(message)
        );

        LOGGER.info("{} initialized with {} modules.", MOD_NAME, moduleManager.getAllModules().size());
    }
}
