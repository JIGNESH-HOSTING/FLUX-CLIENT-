package com.jigneshgaming.fluxclient.ui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * FLUX CLIENT Store & Community Hub screen.
 * Provides instant free access to all FLUX Cosmetics plus official project & release links.
 */
public class FluxStoreScreen extends Screen {
    private static final String REPO_URL = "https://github.com/JIGNESH-HOSTING/FLUX-CLIENT-";
    private final Screen parent;

    public FluxStoreScreen(Screen parent) {
        super(Text.literal("Flux Store"));
        this.parent = parent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x9904070C);

        int pw = Math.min(390, this.width - 24);
        int ph = 188;
        int px = (this.width - pw) / 2;
        int py = (this.height - ph) / 2;

        RenderUtils.drawRoundedBox(ctx, px, py, pw, ph, FluxModMenuScreen.PANEL_BG, 0xFF283546);
        ctx.drawTextWithShadow(this.textRenderer, "FLUX CLIENT STORE & HUB", px + 14, py + 12, 0xFF29B2FF);
        ctx.drawTextWithShadow(this.textRenderer, "Developed by JIGNESHGAMING-k7j — 100% Free & Open Edition", px + 14, py + 24, 0xFF8E9BA8);

        int closeX = px + pw - 26;
        boolean closeHov = RenderUtils.isHovered(mouseX, mouseY, closeX, py + 8, 18, 18);
        RenderUtils.drawRoundedBox(ctx, closeX, py + 8, 18, 18, closeHov ? FluxModMenuScreen.ACCENT_ORANGE : 0xFF161B22, FluxModMenuScreen.ACCENT_ORANGE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "x", closeX + 9, py + 13, 0xFFFFFFFF);

        // Card 1: Free Founder Cosmetics Bundle
        int c1Y = py + 44;
        boolean c1Hov = RenderUtils.isHovered(mouseX, mouseY, px + 12, c1Y, pw - 24, 42);
        RenderUtils.drawRoundedBox(ctx, px + 12, c1Y, pw - 24, 42, FluxModMenuScreen.CARD_BG, c1Hov ? 0xFF3CAEFF : 0xFF24303E);
        ctx.drawTextWithShadow(this.textRenderer, "Founder Capes & Elytra Bundle (FREE)", px + 20, c1Y + 9, 0xFFF2F7FA);
        ctx.drawTextWithShadow(this.textRenderer, "Electric Blue, Inferno Orange, Stealth Carbon & Cyber Gold", px + 20, c1Y + 22, 0xFF8C9AA8);
        RenderUtils.drawRoundedBox(ctx, px + pw - 86, c1Y + 13, 66, 16, 0xFF1F9D4C, 0xFF2ECC71);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "WARDROBE", px + pw - 53, c1Y + 17, 0xFFFFFFFF);

        // Card 2: Official FLUX Releases & Source
        int c2Y = c1Y + 48;
        boolean c2Hov = RenderUtils.isHovered(mouseX, mouseY, px + 12, c2Y, pw - 24, 42);
        RenderUtils.drawRoundedBox(ctx, px + 12, c2Y, pw - 24, 42, FluxModMenuScreen.CARD_BG, c2Hov ? FluxModMenuScreen.ACCENT_ORANGE : 0xFF24303E);
        ctx.drawTextWithShadow(this.textRenderer, "Official FLUX Client Releases & Updates", px + 20, c2Y + 9, 0xFFF2F7FA);
        ctx.drawTextWithShadow(this.textRenderer, "Check GitHub releases for new 1.21.11 updates & changelogs", px + 20, c2Y + 22, 0xFF8C9AA8);
        RenderUtils.drawRoundedBox(ctx, px + pw - 86, c2Y + 13, 66, 16, FluxModMenuScreen.ACCENT_ORANGE, FluxModMenuScreen.ACCENT_ORANGE_HOVER);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "OPEN", px + pw - 53, c2Y + 17, 0xFFFFFFFF);

        // Back button
        int backW = 90;
        int backX = px + (pw - backW) / 2;
        int backY = py + ph - 26;
        boolean backHov = RenderUtils.isHovered(mouseX, mouseY, backX, backY, backW, 18);
        RenderUtils.drawMainMenuButton(ctx, backX, backY, backW, 18, backHov);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Back", backX + backW / 2, backY + 5, 0xFFFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();

        int pw = Math.min(390, this.width - 24);
        int ph = 188;
        int px = (this.width - pw) / 2;
        int py = (this.height - ph) / 2;

        if (RenderUtils.isHovered(mx, my, px + pw - 26, py + 8, 18, 18)
                || !RenderUtils.isHovered(mx, my, px, py, pw, ph)
                || RenderUtils.isHovered(mx, my, px + (pw - 90) / 2, py + ph - 26, 90, 18)) {
            this.close();
            return true;
        }

        if (RenderUtils.isHovered(mx, my, px + 12, py + 44, pw - 24, 42) && this.client != null) {
            this.client.setScreen(new FluxCosmeticsScreen(this));
            return true;
        }
        if (RenderUtils.isHovered(mx, my, px + 12, py + 92, pw - 24, 42)) {
            ConfirmLinkScreen.open(this, REPO_URL, true);
            return true;
        }

        return super.mouseClicked(click, doubled);
    }
}
