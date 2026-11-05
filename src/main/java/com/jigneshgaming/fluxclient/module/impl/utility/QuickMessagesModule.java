package com.jigneshgaming.fluxclient.module.impl.utility;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.StringSetting;
import com.jigneshgaming.fluxclient.ui.FluxNotificationOverlay;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public final class QuickMessagesModule extends Module {
    private final List<StringSetting> slotSettings = new ArrayList<>(6);

    public QuickMessagesModule() {
        super(
                "quick_messages",
                "Quick Messages",
                "Send customizable chat macros or server commands with a single click or G key.",
                ModuleCategory.UTILITY,
                ModuleIcon.QUICK_MESSAGES,
                false
        );
        slotSettings.add(addSetting(new StringSetting("slot1", "Quick Slot 1", "Quick chat or command #1", "gg", 80)));
        slotSettings.add(addSetting(new StringSetting("slot2", "Quick Slot 2", "Quick chat or command #2", "gl hf!", 80)));
        slotSettings.add(addSetting(new StringSetting("slot3", "Quick Slot 3", "Quick chat or command #3", "/hub", 80)));
        slotSettings.add(addSetting(new StringSetting("slot4", "Quick Slot 4", "Quick chat or command #4", "/spawn", 80)));
        slotSettings.add(addSetting(new StringSetting("slot5", "Quick Slot 5", "Quick chat or command #5", "Incoming!", 80)));
        slotSettings.add(addSetting(new StringSetting("slot6", "Quick Slot 6", "Quick chat or command #6", "Well played!", 80)));
    }

    public List<StringSetting> getSlotSettings() {
        return slotSettings;
    }

    public void sendSlot(int index) {
        if (index < 0 || index >= slotSettings.size()) {
            return;
        }
        String content = slotSettings.get(index).getValue().trim();
        if (content.isEmpty()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            FluxNotificationOverlay.push("Quick Messages", "Join a world or server first", FluxNotificationOverlay.Type.WARNING);
            return;
        }
        if (content.startsWith("/")) {
            client.getNetworkHandler().sendChatCommand(content.substring(1));
        } else {
            client.getNetworkHandler().sendChatMessage(content);
        }
        FluxNotificationOverlay.push("Quick Message Sent", content, FluxNotificationOverlay.Type.INFO);
    }
}
