package com.jigneshgaming.fluxclient.ui;

import com.jigneshgaming.fluxclient.cosmetics.CosmeticsManager;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Interactive FLUX CLIENT Cosmetics Wardrobe screen.
 * Allows selecting and equipping client-side FLUX Capes & Elytra textures.
 */
public class FluxCosmeticsScreen extends Screen {
    private final Screen parent;

    public FluxCosmeticsScreen(Screen parent) {
        super(Text.literal("Flux Cosmetics"));
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

        int pw = Math.min(430, this.width - 24);
        int ph = Math.min(236, this.height - 14);
        int px = (this.width - pw) / 2;
        int py = (this.height - ph) / 2;

        RenderUtils.drawRoundedBox(ctx, px, py, pw, ph, FluxModMenuScreen.PANEL_BG, 0xFF283444);
        ctx.drawTextWithShadow(this.textRenderer, "FLUX COSMETICS WARDROBE", px + 12, py + 10, 0xFF29B2FF);
        ctx.drawTextWithShadow(this.textRenderer, "All FLUX 1.21.11 capes & elytra skins unlocked free", px + 12, py + 21, 0xFF8C9AA8);

        int closeX = px + pw - 26;
        boolean closeHov = RenderUtils.isHovered(mouseX, mouseY, closeX, py + 8, 18, 18);
        RenderUtils.drawRoundedBox(ctx, closeX, py + 8, 18, 18, closeHov ? FluxModMenuScreen.ACCENT_ORANGE : 0xFF161B22, FluxModMenuScreen.ACCENT_ORANGE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "x", closeX + 9, py + 13, 0xFFFFFFFF);

        CosmeticsManager.CapeStyle[] styles = CosmeticsManager.CapeStyle.values();
        CosmeticsManager.CapeStyle active = CosmeticsManager.getInstance().getActiveCape();

        boolean compact = ph < 230;
        int rowY = py + (compact ? 35 : 38);
        int rowH = compact ? 31 : 34;
        int gap = compact ? 3 : 4;

        for (int i = 0; i < styles.length; i++) {
            CosmeticsManager.CapeStyle style = styles[i];
            int ry = rowY + i * (rowH + gap);
            boolean isEquipped = (style == active);
            boolean hov = RenderUtils.isHovered(mouseX, mouseY, px + 10, ry, pw - 20, rowH);

            int border = isEquipped ? 0xFF29B2FF : (hov ? 0xFF364558 : FluxModMenuScreen.CARD_BORDER);
            RenderUtils.drawRoundedBox(ctx, px + 10, ry, pw - 20, rowH, FluxModMenuScreen.CARD_BG, border);

            // Cape front-face preview swatch (20x26)
            int prevX = px + 18;
            int prevY = ry + 4;
            Identifier guiTex = style.getGuiTexturePath();
            if (guiTex != null) {
                ctx.drawTexture(
                        RenderPipelines.GUI_TEXTURED,
                        guiTex,
                        prevX, prevY,
                        1.0f, 1.0f,
                        16, 26,
                        10, 16,
                        64, 32,
                        0xFFFFFFFF
                );
                ctx.drawStrokedRectangle(prevX - 1, prevY - 1, 18, 28, style.getPreviewColor());
            } else {
                RenderUtils.drawRoundedBox(ctx, prevX, prevY, 16, 26, 0xFF1A2029, 0xFF394452);
                ctx.drawCenteredTextWithShadow(this.textRenderer, "-", prevX + 8, prevY + 9, 0xFF8894A2);
            }

            ctx.drawTextWithShadow(this.textRenderer, style.getDisplayName(), px + 44, ry + 7, 0xFFF2F7FA);
            ctx.getMatrices().pushMatrix();
            ctx.getMatrices().translate(px + 44, ry + 19);
            ctx.getMatrices().scale(0.8f, 0.8f);
            ctx.drawTextWithShadow(this.textRenderer, style.getDescription(), 0, 0, 0xFF8C9AA8);
            ctx.getMatrices().popMatrix();

            int btnW = 68;
            int btnX = px + pw - 18 - btnW;
            int btnY = ry + 9;
            boolean btnHov = RenderUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, 16);
            int btnFill = isEquipped
                    ? FluxModMenuScreen.ENABLED_GREEN
                    : (btnHov ? 0xFF1868B8 : 0xFF17202B);
            int btnBorder = isEquipped ? 0xFF2ECC71 : (btnHov ? 0xFF47BEFF : 0xFF2E3A4A);
            RenderUtils.drawRoundedBox(ctx, btnX, btnY, btnW, 16, btnFill, btnBorder);
            ctx.drawCenteredTextWithShadow(
                    this.textRenderer,
                    isEquipped ? "EQUIPPED" : "EQUIP",
                    btnX + btnW / 2,
                    btnY + 4,
                    0xFFFFFFFF
            );
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();

        int pw = Math.min(430, this.width - 24);
        int ph = Math.min(236, this.height - 14);
        int px = (this.width - pw) / 2;
        int py = (this.height - ph) / 2;

        if (RenderUtils.isHovered(mx, my, px + pw - 26, py + 8, 18, 18)
                || !RenderUtils.isHovered(mx, my, px, py, pw, ph)) {
            this.close();
            return true;
        }

        CosmeticsManager.CapeStyle[] styles = CosmeticsManager.CapeStyle.values();
        boolean compact = ph < 230;
        int rowY = py + (compact ? 35 : 38);
        int rowH = compact ? 31 : 34;
        int gap = compact ? 3 : 4;
        for (int i = 0; i < styles.length; i++) {
            int ry = rowY + i * (rowH + gap);
            if (RenderUtils.isHovered(mx, my, px + 10, ry, pw - 20, rowH)) {
                CosmeticsManager.getInstance().setActiveCape(styles[i], true);
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }
}
