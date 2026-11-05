package com.jigneshgaming.fluxclient.module.impl.hud;

import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;

import java.util.Collections;

public final class ServerInfoModule extends HudModule {
    private final BooleanSetting showPingSetting;
    private final BooleanSetting hideIpSetting;

    private long lastUpdateMs = 0L;
    private String cachedLine = "Singleplayer";

    public ServerInfoModule() {
        super(
                "server_info",
                "Server Info",
                "Displays connected server address/name and live network ping.",
                ModuleIcon.SERVER_INFO,
                false,
                0.012f,
                0.285f
        );
        this.showPingSetting = addSetting(new BooleanSetting("showPing", "Show Ping", "Append live latency in ms", true));
        this.hideIpSetting = addSetting(new BooleanSetting("hideIp", "Streamer Hide IP", "Mask server IP for streaming privacy", false));
    }

    @Override
    protected void renderContent(DrawContext context, MinecraftClient client, boolean previewMode) {
        long now = System.currentTimeMillis();
        if (now - lastUpdateMs >= 400L || previewMode) {
            lastUpdateMs = now;
            ServerInfo entry = client.getCurrentServerEntry();
            if (entry != null) {
                String label = hideIpSetting.get() ? "Server (Hidden)" : entry.address;
                if (showPingSetting.get()) {
                    int ping = resolveLatency(client, entry);
                    cachedLine = label + " • " + ping + " ms";
                } else {
                    cachedLine = label;
                }
            } else if (client.isInSingleplayer()) {
                cachedLine = "Singleplayer • 0 ms";
            } else {
                cachedLine = previewMode ? "play.fluxclient.net • 24 ms" : "Menu";
            }
        }
        drawHudLineBox(context, client.textRenderer, cachedLine);
    }

    private static int resolveLatency(MinecraftClient client, ServerInfo serverInfo) {
        if (client.player != null && client.getNetworkHandler() != null) {
            PlayerListEntry ple = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (ple != null && ple.getLatency() > 0) {
                return ple.getLatency();
            }
        }
        return serverInfo.ping > 0 ? (int) serverInfo.ping : 0;
    }
}
