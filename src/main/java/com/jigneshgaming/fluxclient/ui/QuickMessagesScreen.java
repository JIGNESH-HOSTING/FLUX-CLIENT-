package com.jigneshgaming.fluxclient.ui;

import com.jigneshgaming.fluxclient.config.FluxConfigManager;
import com.jigneshgaming.fluxclient.module.ModuleManager;
import com.jigneshgaming.fluxclient.module.impl.utility.QuickMessagesModule;
import com.jigneshgaming.fluxclient.module.setting.StringSetting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Interactive Quick Messages macro & command launcher / editor screen.
 */
public class QuickMessagesScreen extends Screen {
    private final Screen parent;
    private int editingSlot = -1;

    public QuickMessagesScreen(Screen parent) {
        super(Text.literal("Quick Messages"));
        this.parent = parent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        FluxConfigManager.saveAsync();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x8805080D);

        int pw = Math.min(360, this.width - 24);
        int ph = 224;
        int px = (this.width - pw) / 2;
        int py = (this.height - ph) / 2;

        RenderUtils.drawRoundedBox(ctx, px, py, pw, ph, FluxModMenuScreen.PANEL_BG, 0xFF283240);
        ctx.drawTextWithShadow(this.textRenderer, "QUICK MESSAGES", px + 12, py + 11, FluxModMenuScreen.ACCENT_ORANGE);
        ctx.drawTextWithShadow(this.textRenderer, "Press 1-6 to send or click text to edit", px + 12, py + 22, 0xFF8C98A6);

        int closeX = px + pw - 26;
        boolean closeHov = RenderUtils.isHovered(mouseX, mouseY, closeX, py + 8, 18, 18);
        RenderUtils.drawRoundedBox(ctx, closeX, py + 8, 18, 18, closeHov ? FluxModMenuScreen.ACCENT_ORANGE : 0xFF161B22, FluxModMenuScreen.ACCENT_ORANGE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "x", closeX + 9, py + 13, 0xFFFFFFFF);

        QuickMessagesModule mod = ModuleManager.getInstance().quickMessages;
        List<StringSetting> slots = mod.getSlotSettings();
        int rowY = py + 38;
        int rowH = 26;
        int gap = 4;

        for (int i = 0; i < slots.size(); i++) {
            int ry = rowY + i * (rowH + gap);
            String val = slots.get(i).getValue();
            boolean focused = (editingSlot == i);

            RenderUtils.drawRoundedBox(ctx, px + 10, ry, pw - 20, rowH, FluxModMenuScreen.CARD_BG, focused ? FluxModMenuScreen.ACCENT_ORANGE : FluxModMenuScreen.CARD_BORDER);

            // Key badge [1]..[6]
            RenderUtils.drawRoundedBox(ctx, px + 16, ry + 5, 18, 16, 0xFF1B222C, 0xFF2E3846);
            ctx.drawCenteredTextWithShadow(this.textRenderer, String.valueOf(i + 1), px + 25, ry + 9, FluxModMenuScreen.ACCENT_ORANGE);

            // Editable message text box
            int textX = px + 40;
            int sendW = 48;
            int sendX = px + pw - 16 - sendW;
            int maxTextW = sendX - textX - 10;
            String shown = RenderUtils.ellipsize(this.textRenderer, val + (focused ? "|" : ""), maxTextW);
            ctx.drawTextWithShadow(this.textRenderer, shown, textX, ry + 9, 0xFFF0F5FA);

            // SEND button
            boolean sendHov = RenderUtils.isHovered(mouseX, mouseY, sendX, ry + 5, sendW, 16);
            RenderUtils.drawRoundedBox(
                    ctx, sendX, ry + 5, sendW, 16,
                    sendHov ? FluxModMenuScreen.ACCENT_ORANGE_HOVER : FluxModMenuScreen.ACCENT_ORANGE,
                    FluxModMenuScreen.ACCENT_ORANGE_HOVER
            );
            ctx.drawCenteredTextWithShadow(this.textRenderer, "SEND", sendX + sendW / 2, ry + 9, 0xFFFFFFFF);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();

        int pw = Math.min(360, this.width - 24);
        int ph = 224;
        int px = (this.width - pw) / 2;
        int py = (this.height - ph) / 2;

        if (RenderUtils.isHovered(mx, my, px + pw - 26, py + 8, 18, 18)
                || !RenderUtils.isHovered(mx, my, px, py, pw, ph)) {
            this.close();
            return true;
        }

        QuickMessagesModule mod = ModuleManager.getInstance().quickMessages;
        List<StringSetting> slots = mod.getSlotSettings();
        int rowY = py + 38;
        int rowH = 26;
        int gap = 4;

        editingSlot = -1;
        for (int i = 0; i < slots.size(); i++) {
            int ry = rowY + i * (rowH + gap);
            int sendW = 48;
            int sendX = px + pw - 16 - sendW;
            if (RenderUtils.isHovered(mx, my, sendX, ry + 5, sendW, 16)) {
                mod.sendSlot(i);
                if (this.client != null && this.client.world != null) {
                    this.client.setScreen(null);
                }
                return true;
            }
            if (RenderUtils.isHovered(mx, my, px + 10, ry, pw - 20, rowH)) {
                editingSlot = i;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            if (editingSlot >= 0) {
                editingSlot = -1;
                FluxConfigManager.saveAsync();
                return true;
            }
            this.close();
            return true;
        }

        QuickMessagesModule mod = ModuleManager.getInstance().quickMessages;
        List<StringSetting> slots = mod.getSlotSettings();

        if (editingSlot >= 0 && editingSlot < slots.size()) {
            StringSetting s = slots.get(editingSlot);
            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE) {
                String cur = s.getValue();
                if (!cur.isEmpty()) {
                    s.setValue(cur.substring(0, cur.length() - 1));
                }
                return true;
            }
            if (input.isEnter()) {
                editingSlot = -1;
                FluxConfigManager.saveAsync();
                return true;
            }
            return true;
        }

        // Hotkeys 1..6 when not editing text
        int num = input.asNumber();
        if (num >= 1 && num <= 6) {
            mod.sendSlot(num - 1);
            if (this.client != null && this.client.world != null) {
                this.client.setScreen(null);
            }
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (editingSlot >= 0 && input.isValidChar()) {
            StringSetting s = ModuleManager.getInstance().quickMessages.getSlotSettings().get(editingSlot);
            s.setValue(s.getValue() + input.asString());
            return true;
        }
        return super.charTyped(input);
    }
}
