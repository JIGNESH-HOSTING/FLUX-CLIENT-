package com.jigneshgaming.fluxclient.ui;

import com.jigneshgaming.fluxclient.FluxClientMod;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.File;

/**
 * Custom FLUX CLIENT Main Menu screen for Minecraft 1.21.11.
 * Features smooth zero-allocation blue hover animations, responsive mobile/desktop layout,
 * and single-pass background rendering optimized for Vivo 1916 (3 GB RAM / PojavLauncher) & PC.
 */
public class FluxMainMenuScreen extends Screen {
    private static final Identifier MENU_BG = Identifier.of(FluxClientMod.MOD_ID, "textures/gui/menu_bg.png");
    private static final Identifier LOGO_F = Identifier.of(FluxClientMod.MOD_ID, "textures/gui/logo_f.png");
    private static final String DISCORD_URL = "https://github.com/JIGNESH-HOSTING/FLUX-CLIENT-";

    private static final String[] BUTTON_LABELS = {
            "Singleplayer",
            "Multiplayer",
            "Cosmetics",
            "Mods",
            "Store",
            "Quit Game"
    };

    private final float[] btnHover = new float[BUTTON_LABELS.length];
    private final float[] topHover = new float[4];
    private float quitTopHover = 0.0f;
    private float discordHover = 0.0f;

    public FluxMainMenuScreen() {
        super(Text.literal("Flux Client"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Rendered cleanly in render() without vanilla's 6-face panorama or Gaussian blur passes.
    }

    private int computeChipWidth(String username) {
        return Math.max(78, 24 + this.textRenderer.getWidth(username) + 10);
    }

    private int computeTopGroupStartX(int chipW) {
        int groupW = chipW + 6 + (4 * 20) + (3 * 4);
        int rightReserve = 52;
        return Math.max(10, this.width - rightReserve - groupW);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // 1. Fullscreen dark desaturated voxel landscape background
        ctx.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                MENU_BG,
                0, 0,
                0.0f, 0.0f,
                this.width, this.height,
                960, 540,
                960, 540,
                0xFFFFFFFF
        );
        ctx.fillGradient(0, 0, this.width, 44, 0x9E04070C, 0x0004070C);
        ctx.fillGradient(0, this.height - 52, this.width, this.height, 0x0004070C, 0xB004070C);

        // 2. Top-Right Player Badge + 4 Quick-Action Icon Buttons + Red Exit Button
        String username = this.client != null && this.client.getSession() != null
                ? this.client.getSession().getUsername()
                : "Player";
        int topY = 8;
        int topH = 19;
        int chipW = computeChipWidth(username);
        int chipX = computeTopGroupStartX(chipW);

        RenderUtils.drawRoundedBox(ctx, chipX, topY, chipW, topH, 0xDC080C12, 0xFF233042);
        RenderUtils.drawPersonIcon(ctx, chipX + 11, topY + 10, 0xFFF0F6FC);
        ctx.drawTextWithShadow(this.textRenderer, username, chipX + 21, topY + 6, 0xFFEAF1F8);

        int topBtnStartX = chipX + chipW + 6;
        for (int i = 0; i < 4; i++) {
            int bx = topBtnStartX + i * 24;
            boolean hov = RenderUtils.isHovered(mouseX, mouseY, bx, topY, 20, topH);
            topHover[i] = RenderUtils.animate(topHover[i], hov, delta, 0.35f);

            int fill = RenderUtils.lerpArgb(0xDC080C12, 0xEE0E2A4D, topHover[i]);
            int border = RenderUtils.lerpArgb(0xFF233042, 0xFF45BAFF, topHover[i]);
            int iconCol = RenderUtils.lerpArgb(0xFFD6E0EC, 0xFFFFFFFF, topHover[i]);
            RenderUtils.drawRoundedBox(ctx, bx, topY, 20, topH, fill, border);

            int icx = bx + 10;
            int icy = topY + 10;
            switch (i) {
                case 0 -> RenderUtils.drawGridPlusIcon(ctx, icx, icy, iconCol);
                case 1 -> RenderUtils.drawChatIcon(ctx, icx, icy, iconCol);
                case 2 -> RenderUtils.drawCameraIcon(ctx, icx, icy, iconCol);
                case 3 -> RenderUtils.drawGearIcon(ctx, icx, icy, iconCol);
            }
        }

        // Far top-right vibrant red Quit button
        int quitTopX = this.width - 28;
        boolean quitTopHov = RenderUtils.isHovered(mouseX, mouseY, quitTopX, topY, 19, topH);
        quitTopHover = RenderUtils.animate(quitTopHover, quitTopHov, delta, 0.35f);
        int qFill = RenderUtils.lerpArgb(0xEBBE2B2B, 0xFFFF4242, quitTopHover);
        int qBorder = RenderUtils.lerpArgb(0xFFE04848, 0xFFFF7A7A, quitTopHover);
        RenderUtils.drawRoundedBox(ctx, quitTopX, topY, 19, topH, qFill, qBorder);
        RenderUtils.drawRunningExitIcon(ctx, quitTopX + 9, topY + 10, 0xFFFFFFFF);

        // 3. Responsive Center Hero Header ([F Logo] FLUX CLIENT) + 6 Vertical Buttons
        boolean compact = this.height < 265;
        int btnW = Math.min(206, Math.max(168, (int) (this.width * 0.25f)));
        int btnH = compact ? 18 : 22;
        int btnGap = compact ? 3 : 4;
        int quitExtraGap = compact ? 5 : 9;
        int headerH = compact ? 34 : 44;

        int stackH = headerH
                + (BUTTON_LABELS.length * btnH)
                + ((BUTTON_LABELS.length - 1) * btnGap)
                + quitExtraGap;
        int startY = Math.max(30, (this.height - stackH) / 2 - 2);
        int centerX = this.width / 2;

        // Side-by-side glowing electric-blue F emblem + FLUX CLIENT wordmark
        int logoSize = compact ? 30 : 38;
        float titleScale = compact ? 1.35f : 1.60f;
        String fluxPart = "FLUX ";
        String clientPart = "CLIENT";
        int fluxW = this.textRenderer.getWidth(fluxPart);
        int clientW = this.textRenderer.getWidth(clientPart);
        int scaledWordW = Math.round((fluxW + clientW) * titleScale);
        int heroTotalW = logoSize + 6 + scaledWordW;
        int heroLeftX = centerX - heroTotalW / 2;

        ctx.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                LOGO_F,
                heroLeftX, startY - 2,
                0.0f, 0.0f,
                logoSize, logoSize,
                256, 256,
                256, 256,
                0xFFFFFFFF
        );

        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(heroLeftX + logoSize + 6, startY + (logoSize - Math.round(8 * titleScale)) / 2 + 1);
        ctx.getMatrices().scale(titleScale, titleScale);
        ctx.drawTextWithShadow(this.textRenderer, fluxPart, 0, 0, 0xFFFFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, clientPart, fluxW, 0, 0xFF29B2FF);
        ctx.getMatrices().popMatrix();

        // 6 Center Vertical Buttons with smooth hover transition
        int btnX = centerX - btnW / 2;
        int curBtnY = startY + headerH;
        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            if (i == 5) {
                curBtnY += quitExtraGap;
            }
            boolean hov = RenderUtils.isHovered(mouseX, mouseY, btnX, curBtnY, btnW, btnH);
            btnHover[i] = RenderUtils.animate(btnHover[i], hov, delta, 0.32f);

            int drawX = btnX;
            RenderUtils.drawMainMenuButton(ctx, drawX, curBtnY, btnW, btnH, btnHover[i]);

            int iconColor = RenderUtils.lerpArgb(0xFFE4ECF6, 0xFFFFFFFF, btnHover[i]);
            int icx = drawX + 20;
            int icy = curBtnY + btnH / 2;
            switch (i) {
                case 0 -> RenderUtils.drawPersonIcon(ctx, icx, icy, iconColor);
                case 1 -> RenderUtils.drawMultiplayerIcon(ctx, icx, icy, iconColor);
                case 2 -> RenderUtils.drawHangerIcon(ctx, icx, icy, iconColor);
                case 3 -> RenderUtils.drawPuzzleIcon(ctx, icx, icy, iconColor);
                case 4 -> RenderUtils.drawStoreIcon(ctx, icx, icy, iconColor);
                case 5 -> RenderUtils.drawQuitDoorIcon(ctx, icx, icy, iconColor);
            }

            int textColor = RenderUtils.lerpArgb(0xFFE8F0F8, 0xFFFFFFFF, btnHover[i]);
            ctx.drawCenteredTextWithShadow(
                    this.textRenderer,
                    BUTTON_LABELS[i],
                    centerX + 5,
                    curBtnY + (btnH - 8) / 2,
                    textColor
            );

            curBtnY += btnH + btnGap;
        }

        // 4. Bottom-Right Discord Invitation Card matching reference design
        int discW = compact ? 116 : 132;
        int discH = compact ? 44 : 54;
        int discX = this.width - discW - 10;
        int discY = this.height - discH - 14;
        boolean discHov = RenderUtils.isHovered(mouseX, mouseY, discX, discY, discW, discH);
        discordHover = RenderUtils.animate(discordHover, discHov, delta, 0.30f);

        RenderUtils.drawDiscordCard(ctx, discX, discY, discW, discH, discordHover);
        RenderUtils.drawDiscordLogo(
                ctx,
                discX + discW / 2,
                discY + (compact ? 14 : 18),
                RenderUtils.lerpArgb(0xFF4CA6FF, 0xFF75C2FF, discordHover)
        );

        float dScale = compact ? 0.72f : 0.80f;
        String joinPrefix = "JOIN OUR ";
        String joinBlue = "DISCORD";
        int line1W = this.textRenderer.getWidth(joinPrefix) + this.textRenderer.getWidth(joinBlue);
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(discX + discW / 2.0f, discY + (compact ? 26 : 33));
        ctx.getMatrices().scale(dScale, dScale);
        int l1Left = -line1W / 2;
        ctx.drawTextWithShadow(this.textRenderer, joinPrefix, l1Left, 0, 0xFFFFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, joinBlue, l1Left + this.textRenderer.getWidth(joinPrefix), 0, 0xFF29B2FF);

        String clickPrefix = "CLICK HERE ";
        String arrowBlue = "->";
        int line2W = this.textRenderer.getWidth(clickPrefix) + this.textRenderer.getWidth(arrowBlue);
        int l2Left = -line2W / 2;
        ctx.drawTextWithShadow(this.textRenderer, clickPrefix, l2Left, 11, 0xFFF2F6FA);
        ctx.drawTextWithShadow(this.textRenderer, arrowBlue, l2Left + this.textRenderer.getWidth(clickPrefix), 11, 0xFF29B2FF);
        ctx.getMatrices().popMatrix();

        // 5. Clean Bottom-Center Version Footer
        ctx.drawCenteredTextWithShadow(
                this.textRenderer,
                "Flux Client 1.21.11",
                centerX,
                this.height - 11,
                0xFFA8B4C2
        );

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0 || this.client == null) {
            return super.mouseClicked(click, doubled);
        }
        double mx = click.x();
        double my = click.y();

        String username = this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int topY = 8;
        int topH = 19;
        int chipW = computeChipWidth(username);
        int chipX = computeTopGroupStartX(chipW);
        int topBtnStartX = chipX + chipW + 6;

        // Top quick-action icon buttons
        for (int i = 0; i < 4; i++) {
            int bx = topBtnStartX + i * 24;
            if (RenderUtils.isHovered(mx, my, bx, topY, 20, topH)) {
                switch (i) {
                    case 0 -> this.client.setScreen(new FluxModMenuScreen(this));
                    case 1 -> this.client.setScreen(new QuickMessagesScreen(this));
                    case 2 -> openScreenshotsFolder();
                    case 3 -> this.client.setScreen(new OptionsScreen(this, this.client.options));
                }
                return true;
            }
        }

        // Far top-right Quit button
        if (RenderUtils.isHovered(mx, my, this.width - 28, topY, 19, topH)) {
            this.client.scheduleStop();
            return true;
        }

        // Center 6 buttons
        boolean compact = this.height < 265;
        int btnW = Math.min(206, Math.max(168, (int) (this.width * 0.25f)));
        int btnH = compact ? 18 : 22;
        int btnGap = compact ? 3 : 4;
        int quitExtraGap = compact ? 5 : 9;
        int headerH = compact ? 34 : 44;
        int stackH = headerH + (BUTTON_LABELS.length * btnH) + ((BUTTON_LABELS.length - 1) * btnGap) + quitExtraGap;
        int startY = Math.max(30, (this.height - stackH) / 2 - 2);
        int btnX = (this.width / 2) - (btnW / 2);

        int curBtnY = startY + headerH;
        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            if (i == 5) {
                curBtnY += quitExtraGap;
            }
            if (RenderUtils.isHovered(mx, my, btnX, curBtnY, btnW, btnH)) {
                switch (i) {
                    case 0 -> this.client.setScreen(new SelectWorldScreen(this));
                    case 1 -> this.client.setScreen(new MultiplayerScreen(this));
                    case 2 -> this.client.setScreen(new FluxCosmeticsScreen(this));
                    case 3 -> this.client.setScreen(new FluxModMenuScreen(this));
                    case 4 -> this.client.setScreen(new FluxStoreScreen(this));
                    case 5 -> this.client.scheduleStop();
                }
                return true;
            }
            curBtnY += btnH + btnGap;
        }

        // Discord CTA card
        int discW = compact ? 116 : 132;
        int discH = compact ? 44 : 54;
        if (RenderUtils.isHovered(mx, my, this.width - discW - 10, this.height - discH - 14, discW, discH)) {
            ConfirmLinkScreen.open(this, DISCORD_URL, true);
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    private void openScreenshotsFolder() {
        if (this.client == null) {
            return;
        }
        try {
            File screenshotsDir = new File(this.client.runDirectory, "screenshots");
            if (!screenshotsDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                screenshotsDir.mkdirs();
            }
            Util.getOperatingSystem().open(screenshotsDir);
        } catch (Exception ignored) {
        }
    }
}
