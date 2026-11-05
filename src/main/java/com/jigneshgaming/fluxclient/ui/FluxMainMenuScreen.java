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
 * Lightweight single-pass rendering optimized for desktop and PojavLauncher / Android Java runtimes.
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
        // Intentionally empty: rendered cleanly inside render() without heavy 3D cube panorama overhead.
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
        // Subtle top/bottom atmospheric vignette
        ctx.fillGradient(0, 0, this.width, 48, 0x9904070C, 0x0004070C);
        ctx.fillGradient(0, this.height - 54, this.width, this.height, 0x0004070C, 0xAA04070C);

        // 2. Top-left player chip + 4 quick-action icon buttons
        String username = this.client != null && this.client.getSession() != null
                ? this.client.getSession().getUsername()
                : "Player";
        int chipX = 10;
        int chipY = 10;
        int chipH = 20;
        int chipW = Math.max(76, 26 + this.textRenderer.getWidth(username) + 10);
        RenderUtils.drawRoundedBox(ctx, chipX, chipY, chipW, chipH, 0xD80A0E15, 0xFF253244);
        RenderUtils.drawPersonIcon(ctx, chipX + 11, chipY + 10, 0xFFE6EEF8);
        ctx.drawTextWithShadow(this.textRenderer, username, chipX + 22, chipY + 6, 0xFFE8EEF5);

        int topBtnStartX = chipX + chipW + 5;
        for (int i = 0; i < 4; i++) {
            int bx = topBtnStartX + i * 24;
            boolean hov = RenderUtils.isHovered(mouseX, mouseY, bx, chipY, 20, chipH);
            RenderUtils.drawRoundedBox(
                    ctx, bx, chipY, 20, chipH,
                    hov ? 0xEE10243E : 0xD80A0E15,
                    hov ? 0xFF3CAEFF : 0xFF253244
            );
            int iconColor = hov ? 0xFF66CCFF : 0xFFD0DAE6;
            int icx = bx + 10;
            int icy = chipY + 10;
            switch (i) {
                case 0 -> RenderUtils.drawGridPlusIcon(ctx, icx, icy, iconColor);
                case 1 -> RenderUtils.drawChatIcon(ctx, icx, icy, iconColor);
                case 2 -> RenderUtils.drawCameraIcon(ctx, icx, icy, iconColor);
                case 3 -> RenderUtils.drawGearIcon(ctx, icx, icy, iconColor);
            }
        }

        // 3. Top-right crimson Quit button
        int quitTopX = this.width - 30;
        int quitTopY = 10;
        boolean quitTopHov = RenderUtils.isHovered(mouseX, mouseY, quitTopX, quitTopY, 20, 20);
        RenderUtils.drawRoundedBox(
                ctx, quitTopX, quitTopY, 20, 20,
                quitTopHov ? 0xEE6E141B : 0xDD3D0D12,
                quitTopHov ? 0xFFFF525B : 0xFF8C222B
        );
        RenderUtils.drawQuitDoorIcon(ctx, quitTopX + 10, quitTopY + 10, quitTopHov ? 0xFFFFFFFF : 0xFFFF888E);

        // 4. Center Hero Branding (Glowing F Logo + FLUX CLIENT wordmark) + 6 Action Buttons
        int btnW = Math.min(200, Math.max(176, this.width / 4));
        int btnH = 22;
        int btnGap = 5;
        int totalStackH = 68 + (BUTTON_LABELS.length * btnH) + ((BUTTON_LABELS.length - 1) * btnGap);
        int startY = Math.max(36, (this.height - totalStackH) / 2);
        int centerX = this.width / 2;

        // Glowing electric-blue F emblem
        int logoSize = 44;
        ctx.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                LOGO_F,
                centerX - logoSize / 2, startY - 4,
                0.0f, 0.0f,
                logoSize, logoSize,
                256, 256,
                256, 256,
                0xFFFFFFFF
        );

        // Scaled "FLUX CLIENT" title with white FLUX and electric-blue CLIENT
        String fluxPart = "FLUX ";
        String clientPart = "CLIENT";
        float titleScale = 1.45f;
        int fluxW = this.textRenderer.getWidth(fluxPart);
        int clientW = this.textRenderer.getWidth(clientPart);
        int totalTitleW = fluxW + clientW;

        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(centerX, startY + 42);
        ctx.getMatrices().scale(titleScale, titleScale);
        int titleLeft = -totalTitleW / 2;
        ctx.drawTextWithShadow(this.textRenderer, fluxPart, titleLeft, 0, 0xFFFFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, clientPart, titleLeft + fluxW, 0, 0xFF29B2FF);
        ctx.getMatrices().popMatrix();

        // 6 Center Vertical Buttons
        int btnX = centerX - btnW / 2;
        int firstBtnY = startY + 66;
        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            int by = firstBtnY + i * (btnH + btnGap);
            boolean hov = RenderUtils.isHovered(mouseX, mouseY, btnX, by, btnW, btnH);
            RenderUtils.drawMainMenuButton(ctx, btnX, by, btnW, btnH, hov);

            int iconColor = hov ? 0xFFFFFFFF : 0xFFD2DCE8;
            int icx = btnX + 16;
            int icy = by + btnH / 2;
            switch (i) {
                case 0 -> RenderUtils.drawPersonIcon(ctx, icx, icy, iconColor);
                case 1 -> RenderUtils.drawMultiplayerIcon(ctx, icx, icy, iconColor);
                case 2 -> RenderUtils.drawHangerIcon(ctx, icx, icy, iconColor);
                case 3 -> RenderUtils.drawPuzzleIcon(ctx, icx, icy, iconColor);
                case 4 -> RenderUtils.drawStoreIcon(ctx, icx, icy, iconColor);
                case 5 -> RenderUtils.drawQuitDoorIcon(ctx, icx, icy, iconColor);
            }

            int textColor = hov ? 0xFFFFFFFF : 0xFFE6EEF6;
            ctx.drawCenteredTextWithShadow(
                    this.textRenderer,
                    BUTTON_LABELS[i],
                    centerX + 4,
                    by + (btnH - 8) / 2,
                    textColor
            );
        }

        // 5. Bottom-Right Discord CTA Card
        int discW = 138;
        int discH = 34;
        int discX = this.width - discW - 10;
        int discY = this.height - discH - 10;
        boolean discHov = RenderUtils.isHovered(mouseX, mouseY, discX, discY, discW, discH);
        RenderUtils.drawDiscordCard(ctx, discX, discY, discW, discH, discHov);
        RenderUtils.drawDiscordLogo(ctx, discX + 20, discY + 17, discHov ? 0xFF66C8FF : 0xFFEAF2FC);

        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(discX + 38, discY + 8);
        ctx.getMatrices().scale(0.85f, 0.85f);
        ctx.drawTextWithShadow(this.textRenderer, "JOIN OUR DISCORD", 0, 0, 0xFFF2F7FD);
        ctx.drawTextWithShadow(this.textRenderer, "CLICK HERE ->", 0, 12, discHov ? 0xFF47BEFF : 0xFF9BAAB9);
        ctx.getMatrices().popMatrix();

        // 6. Bottom-Center Version Label
        ctx.drawCenteredTextWithShadow(
                this.textRenderer,
                "Flux Client 1.21.11",
                centerX,
                this.height - 13,
                0xFF6E7A89
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
        int chipW = Math.max(76, 26 + this.textRenderer.getWidth(username) + 10);
        int topBtnStartX = 10 + chipW + 5;

        // Top-left 4 icon buttons
        for (int i = 0; i < 4; i++) {
            int bx = topBtnStartX + i * 24;
            if (RenderUtils.isHovered(mx, my, bx, 10, 20, 20)) {
                switch (i) {
                    case 0 -> this.client.setScreen(new FluxModMenuScreen(this));
                    case 1 -> this.client.setScreen(new QuickMessagesScreen(this));
                    case 2 -> openScreenshotsFolder();
                    case 3 -> this.client.setScreen(new OptionsScreen(this, this.client.options));
                }
                return true;
            }
        }

        // Top-right Quit button
        if (RenderUtils.isHovered(mx, my, this.width - 30, 10, 20, 20)) {
            this.client.scheduleStop();
            return true;
        }

        // Center 6 buttons
        int btnW = Math.min(200, Math.max(176, this.width / 4));
        int btnH = 22;
        int btnGap = 5;
        int totalStackH = 68 + (BUTTON_LABELS.length * btnH) + ((BUTTON_LABELS.length - 1) * btnGap);
        int startY = Math.max(36, (this.height - totalStackH) / 2);
        int btnX = (this.width / 2) - (btnW / 2);
        int firstBtnY = startY + 66;

        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            int by = firstBtnY + i * (btnH + btnGap);
            if (RenderUtils.isHovered(mx, my, btnX, by, btnW, btnH)) {
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
        }

        // Bottom-right Discord CTA
        int discW = 138;
        int discH = 34;
        if (RenderUtils.isHovered(mx, my, this.width - discW - 10, this.height - discH - 10, discW, discH)) {
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
