package com.jigneshgaming.fluxclient.module.impl.utility;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;
import com.jigneshgaming.fluxclient.module.setting.StringSetting;
import com.jigneshgaming.fluxclient.ui.FluxNotificationOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

public final class AutoGgModule extends Module {
    private static final List<String> TRIGGERS = List.of(
            "1st killer -",
            "1st place -",
            "winner:",
            "winners:",
            "winning team -",
            "winning team:",
            "victory!",
            "game over!",
            "won the game!",
            "duel summary",
            "bedwarsSummary"
    );

    private final StringSetting messageSetting;
    private final NumberSetting cooldownSetting;
    private long lastSentMs = 0L;

    public AutoGgModule() {
        super(
                "auto_gg",
                "Auto GG",
                "Automatically sends your sportsmanship message when a minigame match ends.",
                ModuleCategory.UTILITY,
                ModuleIcon.AUTO_GG,
                false
        );
        this.messageSetting = addSetting(new StringSetting("message", "GG Message", "Chat message sent on match end", "gg", 32));
        this.cooldownSetting = addSetting(new NumberSetting("cooldown", "Cooldown", "Minimum seconds between Auto GG messages", 5, 2, 20, 1, "s"));
    }

    public void onIncomingMessage(Text text) {
        if (!isEnabled() || text == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            return;
        }

        String raw = text.getString();
        if (raw == null || raw.isBlank()) {
            return;
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        // Ignore regular player chat lines starting with <Name>
        if (lower.startsWith("<")) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastSentMs < cooldownSetting.getInt() * 1000L) {
            return;
        }

        for (String trigger : TRIGGERS) {
            if (lower.contains(trigger.toLowerCase(Locale.ROOT))) {
                lastSentMs = now;
                String msg = messageSetting.getValue().trim();
                if (!msg.isEmpty()) {
                    if (msg.startsWith("/")) {
                        client.getNetworkHandler().sendChatCommand(msg.substring(1));
                    } else {
                        client.getNetworkHandler().sendChatMessage(msg);
                    }
                    FluxNotificationOverlay.push("Auto GG", "Sent \"" + msg + "\"", FluxNotificationOverlay.Type.SUCCESS);
                }
                break;
            }
        }
    }
}
