package com.jigneshgaming.fluxclient.ui;

import com.jigneshgaming.fluxclient.config.FluxConfigManager;
import com.jigneshgaming.fluxclient.config.HudPresetManager;
import com.jigneshgaming.fluxclient.module.HudModule;
import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleManager;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.ColorSetting;
import com.jigneshgaming.fluxclient.module.setting.ModeSetting;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;
import com.jigneshgaming.fluxclient.module.setting.Setting;
import com.jigneshgaming.fluxclient.module.setting.StringSetting;
import com.jigneshgaming.fluxclient.optimization.LowEndOptimizer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Signature FLUX CLIENT Orange & Black Mod Menu GUI.
 * Includes Category Filtering, Live Search, 4-Column Module Cards, Per-Module Settings,
 * HUD PRESETS modal (6 presets), RESET ALL modal, and Low-End Device Optimizer panel.
 */
public class FluxModMenuScreen extends Screen {
    public static final int ACCENT_ORANGE = 0xFFEE5A24;
    public static final int ACCENT_ORANGE_HOVER = 0xFFFF6B35;
    public static final int ENABLED_GREEN = 0xFF1F9D4C;
    public static final int ENABLED_GREEN_HOVER = 0xFF26B85A;
    public static final int PANEL_BG = 0xF40B0E12;
    public static final int CARD_BG = 0xFF11151B;
    public static final int CARD_BORDER = 0xFF202630;

    private enum ModalState {
        NONE,
        RESET_ALL,
        HUD_PRESETS,
        MODULE_SETTINGS
    }

    private final Screen parent;
    private ModuleCategory selectedCategory = null; // null == "All"
    private boolean optimizerTabActive = false;
    private String searchQuery = "";
    private boolean searchFocused = false;

    private double scrollOffset = 0.0;
    private double maxScroll = 0.0;
    private double settingsScroll = 0.0;

    private ModalState modalState = ModalState.NONE;
    private Module activeSettingsModule = null;
    private NumberSetting draggingSlider = null;
    private StringSetting editingStringSetting = null;

    // Optimizer slider drag index (-1 = none)
    private int draggingOptSlider = -1;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public FluxModMenuScreen(Screen parent) {
        super(Text.literal("Flux Mod Menu"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(540, this.width - 20);
        this.panelH = Math.min(312, this.height - 20);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
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
        // Rendered explicitly inside render() for crisp dark overlay without double-blur cost.
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Fullscreen dark backdrop
        ctx.fill(0, 0, this.width, this.height, 0x88040609);

        // Main outer panel
        RenderUtils.drawRoundedBox(ctx, panelX, panelY, panelW, panelH, PANEL_BG, 0xFF262D37);

        boolean modalOpen = modalState != ModalState.NONE;
        int effMouseX = modalOpen ? -1000 : mouseX;
        int effMouseY = modalOpen ? -1000 : mouseY;

        renderTopHeader(ctx, effMouseX, effMouseY);

        if (optimizerTabActive) {
            renderOptimizerPanel(ctx, effMouseX, effMouseY);
        } else {
            renderCategoryFilterBar(ctx, effMouseX, effMouseY);
            renderModuleGrid(ctx, effMouseX, effMouseY);
        }

        // Render active modal on top
        if (modalState != ModalState.NONE) {
            ctx.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 0xB805070A);
            switch (modalState) {
                case RESET_ALL -> renderResetModal(ctx, mouseX, mouseY);
                case HUD_PRESETS -> renderPresetsModal(ctx, mouseX, mouseY);
                case MODULE_SETTINGS -> renderModuleSettingsModal(ctx, mouseX, mouseY);
                default -> {
                }
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderTopHeader(DrawContext ctx, int mouseX, int mouseY) {
        int headerY = panelY + 8;
        int modTabX = panelX + 10;
        int modTabW = 74;
        int tabH = 20;

        boolean modTabHov = RenderUtils.isHovered(mouseX, mouseY, modTabX, headerY, modTabW, tabH);
        int modTabFill = !optimizerTabActive
                ? (modTabHov ? ACCENT_ORANGE_HOVER : ACCENT_ORANGE)
                : (modTabHov ? 0xFF1E242D : 0xFF151920);
        RenderUtils.drawRoundedBox(ctx, modTabX, headerY, modTabW, tabH, modTabFill, !optimizerTabActive ? ACCENT_ORANGE_HOVER : CARD_BORDER);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "MOD MENU", modTabX + modTabW / 2, headerY + 6, 0xFFFFFFFF);

        // 6 top icon buttons matching reference screenshots: Settings, HUD Editor, Cosmetics, Optimizer, Quick Chat, Presets
        int iconStartX = modTabX + modTabW + 6;
        for (int i = 0; i < 6; i++) {
            int bx = iconStartX + i * 24;
            boolean active = (i == 3 && optimizerTabActive);
            boolean hov = RenderUtils.isHovered(mouseX, mouseY, bx, headerY, 20, tabH);
            int fill = active ? ACCENT_ORANGE : (hov ? 0xFF1F2630 : 0xFF151920);
            int border = active ? ACCENT_ORANGE_HOVER : (hov ? ACCENT_ORANGE : 0xFF262D38);
            RenderUtils.drawRoundedBox(ctx, bx, headerY, 20, tabH, fill, border);

            int ic = (active || hov) ? 0xFFFFFFFF : 0xFFB8C2CE;
            int icx = bx + 10;
            int icy = headerY + 10;
            switch (i) {
                case 0 -> RenderUtils.drawGearIcon(ctx, icx, icy, ic);
                case 1 -> RenderUtils.drawMonitorIcon(ctx, icx, icy, ic);
                case 2 -> RenderUtils.drawHangerIcon(ctx, icx, icy, ic);
                case 3 -> RenderUtils.drawGaugeIcon(ctx, icx, icy, ic);
                case 4 -> RenderUtils.drawChatIcon(ctx, icx, icy, ic);
                case 5 -> RenderUtils.drawPersonIcon(ctx, icx, icy, ic);
            }
        }

        // Top-right orange-bordered [X] close button
        int closeX = panelX + panelW - 30;
        boolean closeHov = RenderUtils.isHovered(mouseX, mouseY, closeX, headerY, 20, tabH);
        RenderUtils.drawRoundedBox(
                ctx, closeX, headerY, 20, tabH,
                closeHov ? ACCENT_ORANGE : 0xFF151920,
                ACCENT_ORANGE
        );
        ctx.drawCenteredTextWithShadow(this.textRenderer, "x", closeX + 10, headerY + 6, 0xFFFFFFFF);

        // Header divider line
        ctx.fill(panelX + 8, panelY + 33, panelX + panelW - 8, panelY + 34, 0xFF1D232C);
    }

    private void renderCategoryFilterBar(DrawContext ctx, int mouseX, int mouseY) {
        int barY = panelY + 39;
        int pillH = 18;
        int cursorX = panelX + 10;

        String[] catNames = {"All", "HUD", "Render", "Movement", "Player", "Utility"};
        ModuleCategory[] cats = {
                null,
                ModuleCategory.HUD,
                ModuleCategory.RENDER,
                ModuleCategory.MOVEMENT,
                ModuleCategory.PLAYER,
                ModuleCategory.UTILITY
        };

        for (int i = 0; i < catNames.length; i++) {
            int pw = this.textRenderer.getWidth(catNames[i]) + 14;
            boolean selected = (selectedCategory == cats[i]);
            boolean hov = RenderUtils.isHovered(mouseX, mouseY, cursorX, barY, pw, pillH);
            int fill = selected ? ACCENT_ORANGE : (hov ? 0xFF1D232B : 0xFF14181F);
            int border = selected ? ACCENT_ORANGE_HOVER : (hov ? 0xFF343E4D : 0xFF232A34);
            RenderUtils.drawRoundedBox(ctx, cursorX, barY, pw, pillH, fill, border);
            ctx.drawCenteredTextWithShadow(
                    this.textRenderer,
                    catNames[i],
                    cursorX + pw / 2,
                    barY + 5,
                    selected ? 0xFFFFFFFF : 0xFFD0D8E2
            );
            cursorX += pw + 4;
        }

        // Right-aligned Search box, Presets button, Reset button
        int searchW = Math.max(76, Math.min(112, panelX + panelW - 10 - (cursorX + 98)));
        int searchX = panelX + panelW - 10 - searchW;
        boolean searchHov = RenderUtils.isHovered(mouseX, mouseY, searchX, barY, searchW, pillH);
        RenderUtils.drawRoundedBox(
                ctx, searchX, barY, searchW, pillH,
                0xFF12161C,
                searchFocused ? ACCENT_ORANGE : (searchHov ? 0xFF3A4554 : 0xFF252C37)
        );
        String displaySearch = searchQuery.isEmpty()
                ? (searchFocused ? "|" : "Search")
                : RenderUtils.ellipsize(this.textRenderer, searchQuery + (searchFocused ? "|" : ""), searchW - 12);
        int searchColor = searchQuery.isEmpty() && !searchFocused ? 0xFF7D8896 : 0xFFF0F5FA;
        ctx.drawTextWithShadow(this.textRenderer, displaySearch, searchX + 6, barY + 5, searchColor);

        int presetsW = 46;
        int presetsX = searchX - presetsW - 4;
        boolean presetsHov = RenderUtils.isHovered(mouseX, mouseY, presetsX, barY, presetsW, pillH);
        RenderUtils.drawRoundedBox(
                ctx, presetsX, barY, presetsW, pillH,
                presetsHov ? 0xFF222933 : 0xFF151A21,
                presetsHov ? ACCENT_ORANGE : 0xFF262E3A
        );
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Presets", presetsX + presetsW / 2, barY + 5, 0xFFE4ECF5);

        int resetW = 38;
        int resetX = presetsX - resetW - 4;
        if (resetX >= cursorX) {
            boolean resetHov = RenderUtils.isHovered(mouseX, mouseY, resetX, barY, resetW, pillH);
            RenderUtils.drawRoundedBox(
                    ctx, resetX, barY, resetW, pillH,
                    resetHov ? 0xFF222933 : 0xFF151A21,
                    resetHov ? ACCENT_ORANGE : 0xFF262E3A
            );
            ctx.drawCenteredTextWithShadow(this.textRenderer, "Reset", resetX + resetW / 2, barY + 5, 0xFFE4ECF5);
        }
    }

    private void renderModuleGrid(DrawContext ctx, int mouseX, int mouseY) {
        List<Module> modules = ModuleManager.getInstance().getFiltered(selectedCategory, searchQuery);

        int gridX = panelX + 10;
        int gridY = panelY + 63;
        int gridW = panelW - 24;
        int gridH = panelH - 72;

        int cols = gridW >= 420 ? 4 : (gridW >= 300 ? 3 : 2);
        int gap = 7;
        int cardW = (gridW - (cols - 1) * gap) / cols;
        int cardH = 84;

        int rows = (modules.size() + cols - 1) / cols;
        int contentH = Math.max(0, rows * cardH + Math.max(0, rows - 1) * gap);
        this.maxScroll = Math.max(0.0, contentH - gridH);
        if (this.scrollOffset > this.maxScroll) {
            this.scrollOffset = this.maxScroll;
        }
        if (this.scrollOffset < 0.0) {
            this.scrollOffset = 0.0;
        }

        ctx.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);

        if (modules.isEmpty()) {
            ctx.drawCenteredTextWithShadow(
                    this.textRenderer,
                    "No modules match \"" + searchQuery + "\"",
                    gridX + gridW / 2,
                    gridY + gridH / 2 - 4,
                    0xFF7D8896
            );
        }

        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            int col = i % cols;
            int row = i / cols;

            int cx = gridX + col * (cardW + gap);
            int cy = gridY + row * (cardH + gap) - (int) Math.round(scrollOffset);
            if (cy + cardH < gridY || cy > gridY + gridH) {
                continue;
            }

            boolean cardHov = RenderUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH)
                    && RenderUtils.isHovered(mouseX, mouseY, gridX, gridY, gridW, gridH);
            RenderUtils.drawRoundedBox(
                    ctx, cx, cy, cardW, cardH,
                    CARD_BG,
                    cardHov ? 0xFF313B49 : CARD_BORDER
            );

            // Signature top orange highlight strip when enabled
            if (mod.isEnabled()) {
                ctx.fill(cx + 1, cy, cx + cardW - 1, cy + 2, ACCENT_ORANGE);
            }

            // Module Name
            String title = RenderUtils.ellipsize(this.textRenderer, mod.getName(), cardW - 10);
            ctx.drawCenteredTextWithShadow(this.textRenderer, title, cx + cardW / 2, cy + 8, 0xFFF2F6FA);

            // Centered Custom Module Icon
            int iconColor = mod.isEnabled() ? 0xFFFFFFFF : 0xFFC5CFD9;
            RenderUtils.drawModuleIcon(ctx, this.textRenderer, mod.getIcon(), cx + cardW / 2, cy + 38, iconColor);

            // Bottom Action Row: [Gear] + [Enabled / Disabled]
            int btnY = cy + cardH - 22;
            int gearX = cx + 6;
            int gearW = 18;
            int btnH = 16;
            boolean gearHov = cardHov && RenderUtils.isHovered(mouseX, mouseY, gearX, btnY, gearW, btnH);
            RenderUtils.drawRoundedBox(
                    ctx, gearX, btnY, gearW, btnH,
                    gearHov ? 0xFF242C37 : 0xFF181D25,
                    gearHov ? ACCENT_ORANGE : 0xFF29313E
            );
            RenderUtils.drawGearIcon(ctx, gearX + gearW / 2, btnY + btnH / 2, gearHov ? 0xFFFFFFFF : 0xFFAAB5C2);

            int togX = gearX + gearW + 4;
            int togW = cardW - 12 - gearW - 4;
            boolean togHov = cardHov && RenderUtils.isHovered(mouseX, mouseY, togX, btnY, togW, btnH);
            int togFill = mod.isEnabled()
                    ? (togHov ? ENABLED_GREEN_HOVER : ENABLED_GREEN)
                    : (togHov ? 0xFF232A34 : 0xFF181D24);
            int togBorder = mod.isEnabled() ? 0xFF2ECC71 : 0xFF28303C;
            RenderUtils.drawRoundedBox(ctx, togX, btnY, togW, btnH, togFill, togBorder);
            ctx.drawCenteredTextWithShadow(
                    this.textRenderer,
                    mod.isEnabled() ? "Enabled" : "Disabled",
                    togX + togW / 2,
                    btnY + 4,
                    mod.isEnabled() ? 0xFFFFFFFF : 0xFFB8C2CE
            );
        }

        ctx.disableScissor();

        // Orange scrollbar on right edge matching Screenshots 3 & 4
        if (this.maxScroll > 0.0) {
            int trackX = panelX + panelW - 8;
            ctx.fill(trackX, gridY, trackX + 3, gridY + gridH, 0xFF181D24);
            int thumbH = Math.max(24, (int) ((double) gridH * gridH / (double) contentH));
            int thumbY = gridY + (int) ((scrollOffset / maxScroll) * (gridH - thumbH));
            ctx.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, ACCENT_ORANGE);
        }
    }

    private void renderOptimizerPanel(DrawContext ctx, int mouseX, int mouseY) {
        LowEndOptimizer opt = LowEndOptimizer.getInstance();
        int x = panelX + 14;
        int y = panelY + 42;
        int w = panelW - 28;

        ctx.drawTextWithShadow(this.textRenderer, "LOW-END & POJAVLAUNCHER OPTIMIZER", x, y, ACCENT_ORANGE);
        ctx.drawTextWithShadow(
                this.textRenderer,
                "Real GPU/CPU draw-call & particle culling with zero fake FPS counters.",
                x, y + 12, 0xFF9AA6B4
        );

        // One-click Low-End Android Profile button
        int profBtnW = 158;
        int profBtnX = x + w - profBtnW;
        boolean profHov = RenderUtils.isHovered(mouseX, mouseY, profBtnX, y, profBtnW, 20);
        RenderUtils.drawRoundedBox(
                ctx, profBtnX, y, profBtnW, 20,
                profHov ? ACCENT_ORANGE_HOVER : ACCENT_ORANGE,
                ACCENT_ORANGE_HOVER
        );
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Apply Low-End Profile", profBtnX + profBtnW / 2, y + 6, 0xFFFFFFFF);

        int rowY = y + 32;
        int rowH = 26;
        int gap = 5;

        drawOptimizerToggleRow(ctx, x, rowY, w, rowH, "Entity Distance Culling",
                "Skips distant passive/ambient/item entities while keeping players & bosses visible",
                opt.isEntityCullingEnabled(), mouseX, mouseY);

        rowY += rowH + gap;
        drawOptimizerSliderRow(ctx, x, rowY, w, rowH, "Passive Entity Max Distance",
                opt.getPassiveEntityMaxDistance() + " blocks",
                (opt.getPassiveEntityMaxDistance() - 16.0) / (96.0 - 16.0));

        rowY += rowH + gap;
        drawOptimizerSliderRow(ctx, x, rowY, w, rowH, "Dropped Item / XP Max Distance",
                opt.getItemEntityMaxDistance() + " blocks",
                (opt.getItemEntityMaxDistance() - 12.0) / (64.0 - 12.0));

        rowY += rowH + gap;
        drawOptimizerToggleRow(ctx, x, rowY, w, rowH, "Particle Burst & Distance Limiter",
                "Prevents TNT, crystal, and weather particle floods from stalling mobile GPUs",
                opt.isParticleLimiterEnabled(), mouseX, mouseY);

        rowY += rowH + gap;
        drawOptimizerSliderRow(ctx, x, rowY, w, rowH, "Max New Particles Per Tick",
                opt.getMaxParticlesPerTick() + " / tick",
                (opt.getMaxParticlesPerTick() - 10.0) / (200.0 - 10.0));

        rowY += rowH + gap;
        drawOptimizerSliderRow(ctx, x, rowY, w, rowH, "Particle Max Render Distance",
                opt.getParticleMaxDistance() + " blocks",
                (opt.getParticleMaxDistance() - 12.0) / (64.0 - 12.0));

        rowY += rowH + gap;
        drawOptimizerToggleRow(ctx, x, rowY, w, rowH, "Unfocused Window FPS Limiter",
                "Caps background frame rate (" + opt.getUnfocusedMaxFps() + " FPS) to reduce battery & thermal throttling",
                opt.isUnfocusedFpsLimiter(), mouseX, mouseY);
    }

    private void drawOptimizerToggleRow(DrawContext ctx, int x, int y, int w, int h,
                                        String title, String subtitle, boolean enabled, int mouseX, int mouseY) {
        RenderUtils.drawRoundedBox(ctx, x, y, w, h, CARD_BG, CARD_BORDER);
        ctx.drawTextWithShadow(this.textRenderer, title, x + 8, y + 4, 0xFFF0F5FA);
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(x + 8, y + 15);
        ctx.getMatrices().scale(0.8f, 0.8f);
        ctx.drawTextWithShadow(this.textRenderer, subtitle, 0, 0, 0xFF8894A2);
        ctx.getMatrices().popMatrix();

        int bx = x + w - 64;
        int by = y + 5;
        boolean hov = RenderUtils.isHovered(mouseX, mouseY, bx, by, 56, 16);
        RenderUtils.drawRoundedBox(
                ctx, bx, by, 56, 16,
                enabled ? (hov ? ENABLED_GREEN_HOVER : ENABLED_GREEN) : (hov ? 0xFF242C37 : 0xFF181D24),
                enabled ? 0xFF2ECC71 : 0xFF2B3340
        );
        ctx.drawCenteredTextWithShadow(this.textRenderer, enabled ? "Enabled" : "Disabled", bx + 28, by + 4, 0xFFFFFFFF);
    }

    private void drawOptimizerSliderRow(DrawContext ctx, int x, int y, int w, int h,
                                        String title, String valLabel, double ratio) {
        RenderUtils.drawRoundedBox(ctx, x, y, w, h, CARD_BG, CARD_BORDER);
        ctx.drawTextWithShadow(this.textRenderer, title, x + 8, y + 9, 0xFFE6EEF6);

        int sliderW = 130;
        int sliderX = x + w - sliderW - 10;
        int sliderY = y + 14;
        ctx.drawTextWithShadow(this.textRenderer, valLabel, sliderX - this.textRenderer.getWidth(valLabel) - 8, y + 9, ACCENT_ORANGE);

        ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 4, 0xFF222933);
        int fillW = (int) Math.round(Math.max(0.0, Math.min(1.0, ratio)) * sliderW);
        ctx.fill(sliderX, sliderY, sliderX + fillW, sliderY + 4, ACCENT_ORANGE);
        ctx.fill(sliderX + fillW - 2, sliderY - 2, sliderX + fillW + 3, sliderY + 6, 0xFFFFFFFF);
    }

    // ==================== MODALS ====================

    private void renderResetModal(DrawContext ctx, int mouseX, int mouseY) {
        int mw = 276;
        int mh = 104;
        int mx = (this.width - mw) / 2;
        int my = (this.height - mh) / 2;

        RenderUtils.drawRoundedBox(ctx, mx, my, mw, mh, 0xFA0E1217, 0xFF2C3542);
        ctx.drawTextWithShadow(this.textRenderer, "RESET ALL", mx + 12, my + 11, 0xFFF2F6FA);

        int closeX = mx + mw - 26;
        int closeY = my + 7;
        boolean closeHov = RenderUtils.isHovered(mouseX, mouseY, closeX, closeY, 18, 18);
        RenderUtils.drawRoundedBox(ctx, closeX, closeY, 18, 18, closeHov ? ACCENT_ORANGE : 0xFF161B22, ACCENT_ORANGE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "x", closeX + 9, closeY + 5, 0xFFFFFFFF);

        int cardW = (mw - 30) / 2;
        int cardH = 56;
        int cardY = my + 34;

        // Left card: DEFAULTS (green border matching Screenshot 9)
        int defX = mx + 10;
        boolean defHov = RenderUtils.isHovered(mouseX, mouseY, defX, cardY, cardW, cardH);
        RenderUtils.drawRoundedBox(
                ctx, defX, cardY, cardW, cardH,
                defHov ? 0xFF14261D : 0xFF111816,
                defHov ? 0xFF39E079 : 0xFF249E52
        );
        ctx.drawCenteredTextWithShadow(this.textRenderer, "DEFAULTS", defX + cardW / 2, cardY + 15, 0xFFF2F7FA);
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(defX + cardW / 2, cardY + 32);
        ctx.getMatrices().scale(0.8f, 0.8f);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Restore original setup", 0, 0, 0xFF9AA8B6);
        ctx.getMatrices().popMatrix();

        // Right card: REMOVE ALL (crimson border matching Screenshot 9)
        int remX = defX + cardW + 10;
        boolean remHov = RenderUtils.isHovered(mouseX, mouseY, remX, cardY, cardW, cardH);
        RenderUtils.drawRoundedBox(
                ctx, remX, cardY, cardW, cardH,
                remHov ? 0xFF2A1417 : 0xFF191215,
                remHov ? 0xFFFF525B : 0xFFB8323A
        );
        ctx.drawCenteredTextWithShadow(this.textRenderer, "REMOVE ALL", remX + cardW / 2, cardY + 15, 0xFFF2F7FA);
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(remX + cardW / 2, cardY + 32);
        ctx.getMatrices().scale(0.8f, 0.8f);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Disable every module", 0, 0, 0xFF9AA8B6);
        ctx.getMatrices().popMatrix();
    }

    private void renderPresetsModal(DrawContext ctx, int mouseX, int mouseY) {
        int mw = Math.min(390, this.width - 28);
        int mh = 186;
        int mx = (this.width - mw) / 2;
        int my = (this.height - mh) / 2;

        RenderUtils.drawRoundedBox(ctx, mx, my, mw, mh, 0xFA0E1217, 0xFF2C3542);
        ctx.drawTextWithShadow(this.textRenderer, "HUD PRESETS", mx + 12, my + 11, 0xFFF2F6FA);

        // Save Custom snapshot button
        int saveBtnW = 74;
        int saveBtnX = mx + mw - 26 - saveBtnW - 6;
        boolean saveHov = RenderUtils.isHovered(mouseX, mouseY, saveBtnX, my + 7, saveBtnW, 18);
        RenderUtils.drawRoundedBox(ctx, saveBtnX, my + 7, saveBtnW, 18, saveHov ? 0xFF222A35 : 0xFF161B22, saveHov ? ACCENT_ORANGE : 0xFF2B3442);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Save Custom", saveBtnX + saveBtnW / 2, my + 12, 0xFFDCE6F2);

        int closeX = mx + mw - 26;
        int closeY = my + 7;
        boolean closeHov = RenderUtils.isHovered(mouseX, mouseY, closeX, closeY, 18, 18);
        RenderUtils.drawRoundedBox(ctx, closeX, closeY, 18, 18, closeHov ? ACCENT_ORANGE : 0xFF161B22, ACCENT_ORANGE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "x", closeX + 9, closeY + 5, 0xFFFFFFFF);

        List<HudPresetManager.PresetInfo> presets = HudPresetManager.PRESETS;
        int cols = 2;
        int gapX = 8;
        int gapY = 7;
        int cardW = (mw - 24 - gapX) / cols;
        int cardH = 42;
        int startY = my + 32;

        for (int i = 0; i < presets.size(); i++) {
            HudPresetManager.PresetInfo p = presets.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx = mx + 12 + col * (cardW + gapX);
            int cy = startY + row * (cardH + gapY);

            boolean hov = RenderUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH);
            RenderUtils.drawRoundedBox(ctx, cx, cy, cardW, cardH, 0xFF131820, hov ? 0xFF364252 : 0xFF232B36);

            // Number badge (01 .. 06)
            RenderUtils.drawRoundedBox(ctx, cx + 6, cy + 6, 18, 14, 0xFF1B222C, 0xFF2B3544);
            ctx.drawCenteredTextWithShadow(this.textRenderer, p.code(), cx + 15, cy + 9, ACCENT_ORANGE);

            // Preset Title
            ctx.drawTextWithShadow(this.textRenderer, p.name(), cx + 28, cy + 9, 0xFFF2F6FA);

            // Subtitle
            ctx.getMatrices().pushMatrix();
            ctx.getMatrices().translate(cx + 7, cy + 26);
            ctx.getMatrices().scale(0.78f, 0.78f);
            ctx.drawTextWithShadow(this.textRenderer, p.subtitle(), 0, 0, 0xFF8C99A8);
            ctx.getMatrices().popMatrix();

            // APPLY button
            int applyW = 40;
            int applyH = 16;
            int applyX = cx + cardW - applyW - 6;
            int applyY = cy + 6;
            boolean applyHov = RenderUtils.isHovered(mouseX, mouseY, applyX, applyY, applyW, applyH);
            RenderUtils.drawRoundedBox(
                    ctx, applyX, applyY, applyW, applyH,
                    applyHov ? ACCENT_ORANGE : 0xFF1A212B,
                    applyHov ? ACCENT_ORANGE_HOVER : 0xFF303A48
            );
            ctx.getMatrices().pushMatrix();
            ctx.getMatrices().translate(applyX + applyW / 2, applyY + 5);
            ctx.getMatrices().scale(0.82f, 0.82f);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "APPLY", 0, 0, 0xFFFFFFFF);
            ctx.getMatrices().popMatrix();
        }
    }

    private void renderModuleSettingsModal(DrawContext ctx, int mouseX, int mouseY) {
        if (activeSettingsModule == null) {
            modalState = ModalState.NONE;
            return;
        }
        int mw = Math.min(360, this.width - 30);
        int mh = Math.min(230, this.height - 30);
        int mx = (this.width - mw) / 2;
        int my = (this.height - mh) / 2;

        RenderUtils.drawRoundedBox(ctx, mx, my, mw, mh, 0xFA0E1217, 0xFF2E3846);
        ctx.drawTextWithShadow(this.textRenderer, activeSettingsModule.getName().toUpperCase() + " SETTINGS", mx + 12, my + 10, ACCENT_ORANGE);
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(mx + 12, my + 21);
        ctx.getMatrices().scale(0.8f, 0.8f);
        ctx.drawTextWithShadow(this.textRenderer, activeSettingsModule.getDescription(), 0, 0, 0xFF8E9BA8);
        ctx.getMatrices().popMatrix();

        // If HUD module, offer "Move HUD" button
        if (activeSettingsModule instanceof HudModule && this.client != null) {
            int moveW = 58;
            int moveX = mx + mw - 26 - moveW - 6;
            boolean moveHov = RenderUtils.isHovered(mouseX, mouseY, moveX, my + 7, moveW, 18);
            RenderUtils.drawRoundedBox(ctx, moveX, my + 7, moveW, 18, moveHov ? ACCENT_ORANGE : 0xFF171D25, ACCENT_ORANGE);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "Move HUD", moveX + moveW / 2, my + 12, 0xFFFFFFFF);
        }

        int closeX = mx + mw - 26;
        int closeY = my + 7;
        boolean closeHov = RenderUtils.isHovered(mouseX, mouseY, closeX, closeY, 18, 18);
        RenderUtils.drawRoundedBox(ctx, closeX, closeY, 18, 18, closeHov ? ACCENT_ORANGE : 0xFF161B22, ACCENT_ORANGE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "x", closeX + 9, closeY + 5, 0xFFFFFFFF);

        List<Setting<?>> settings = activeSettingsModule.getSettings();
        int listX = mx + 10;
        int listY = my + 34;
        int listW = mw - 20;
        int listH = mh - 44;
        int rowH = 24;
        int gap = 4;

        int totalH = settings.size() * (rowH + gap);
        double maxSetScroll = Math.max(0.0, totalH - listH);
        if (settingsScroll > maxSetScroll) settingsScroll = maxSetScroll;
        if (settingsScroll < 0.0) settingsScroll = 0.0;

        ctx.enableScissor(listX, listY, listX + listW, listY + listH);

        for (int i = 0; i < settings.size(); i++) {
            Setting<?> s = settings.get(i);
            int ry = listY + i * (rowH + gap) - (int) Math.round(settingsScroll);
            if (ry + rowH < listY || ry > listY + listH) continue;

            RenderUtils.drawRoundedBox(ctx, listX, ry, listW, rowH, 0xFF141920, 0xFF242C38);
            ctx.drawTextWithShadow(this.textRenderer, s.getName(), listX + 8, ry + 8, 0xFFE6EEF6);

            if (s instanceof BooleanSetting bs) {
                int bx = listX + listW - 54;
                int by = ry + 4;
                boolean hov = RenderUtils.isHovered(mouseX, mouseY, bx, by, 46, 16);
                RenderUtils.drawRoundedBox(
                        ctx, bx, by, 46, 16,
                        bs.getValue() ? (hov ? ENABLED_GREEN_HOVER : ENABLED_GREEN) : (hov ? 0xFF252E3A : 0xFF1A2029),
                        bs.getValue() ? 0xFF2ECC71 : 0xFF2D3644
                );
                ctx.drawCenteredTextWithShadow(this.textRenderer, bs.getValue() ? "ON" : "OFF", bx + 23, by + 4, 0xFFFFFFFF);
            } else if (s instanceof ModeSetting ms) {
                String label = ms.getValue();
                int bw = Math.max(66, this.textRenderer.getWidth(label) + 14);
                int bx = listX + listW - bw - 8;
                int by = ry + 4;
                boolean hov = RenderUtils.isHovered(mouseX, mouseY, bx, by, bw, 16);
                RenderUtils.drawRoundedBox(ctx, bx, by, bw, 16, hov ? 0xFF232B36 : 0xFF192029, hov ? ACCENT_ORANGE : 0xFF2E3846);
                ctx.drawCenteredTextWithShadow(this.textRenderer, label, bx + bw / 2, by + 4, ACCENT_ORANGE);
            } else if (s instanceof ColorSetting cs) {
                String label = cs.getValue();
                int bw = Math.max(76, this.textRenderer.getWidth(label) + 24);
                int bx = listX + listW - bw - 8;
                int by = ry + 4;
                boolean hov = RenderUtils.isHovered(mouseX, mouseY, bx, by, bw, 16);
                RenderUtils.drawRoundedBox(ctx, bx, by, bw, 16, hov ? 0xFF232B36 : 0xFF192029, hov ? ACCENT_ORANGE : 0xFF2E3846);
                ctx.fill(bx + 5, by + 4, bx + 13, by + 12, cs.resolveArgb(255));
                ctx.drawTextWithShadow(this.textRenderer, label, bx + 17, by + 4, 0xFFF0F5FA);
            } else if (s instanceof NumberSetting ns) {
                int sliderW = 96;
                int sliderX = listX + listW - sliderW - 8;
                int sliderY = ry + 11;
                String valStr = ns.formatValue();
                ctx.drawTextWithShadow(this.textRenderer, valStr, sliderX - this.textRenderer.getWidth(valStr) - 6, ry + 8, ACCENT_ORANGE);

                ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 3, 0xFF232B36);
                double norm = (ns.getValue() - ns.getMin()) / Math.max(0.0001, ns.getMax() - ns.getMin());
                int fw = (int) Math.round(norm * sliderW);
                ctx.fill(sliderX, sliderY, sliderX + fw, sliderY + 3, ACCENT_ORANGE);
                ctx.fill(sliderX + fw - 2, sliderY - 2, sliderX + fw + 2, sliderY + 5, 0xFFFFFFFF);
            } else if (s instanceof StringSetting ss) {
                int boxW = 132;
                int bx = listX + listW - boxW - 8;
                int by = ry + 4;
                boolean focused = (editingStringSetting == ss);
                RenderUtils.drawRoundedBox(ctx, bx, by, boxW, 16, 0xFF10141A, focused ? ACCENT_ORANGE : 0xFF2D3644);
                String txt = RenderUtils.ellipsize(this.textRenderer, ss.getValue() + (focused ? "|" : ""), boxW - 10);
                ctx.drawTextWithShadow(this.textRenderer, txt, bx + 5, by + 4, 0xFFF0F5FA);
            }
        }

        ctx.disableScissor();
    }

    // ==================== INPUT HANDLING ====================

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();

        if (modalState != ModalState.NONE) {
            return handleModalClick(mx, my, button);
        }

        // Top header bar
        int headerY = panelY + 8;
        int modTabX = panelX + 10;
        if (RenderUtils.isHovered(mx, my, modTabX, headerY, 74, 20)) {
            optimizerTabActive = false;
            return true;
        }

        int iconStartX = modTabX + 80;
        for (int i = 0; i < 6; i++) {
            int bx = iconStartX + i * 24;
            if (RenderUtils.isHovered(mx, my, bx, headerY, 20, 20)) {
                if (this.client == null) return true;
                switch (i) {
                    case 0 -> this.client.setScreen(new OptionsScreen(this, this.client.options));
                    case 1 -> this.client.setScreen(new HudEditorScreen(this));
                    case 2 -> this.client.setScreen(new FluxCosmeticsScreen(this));
                    case 3 -> optimizerTabActive = !optimizerTabActive;
                    case 4 -> this.client.setScreen(new QuickMessagesScreen(this));
                    case 5 -> modalState = ModalState.HUD_PRESETS;
                }
                return true;
            }
        }

        if (RenderUtils.isHovered(mx, my, panelX + panelW - 30, headerY, 20, 20)) {
            this.close();
            return true;
        }

        if (optimizerTabActive) {
            return handleOptimizerClick(mx, my);
        }

        // Category bar & Search / Presets / Reset
        int barY = panelY + 39;
        int pillH = 18;
        int cursorX = panelX + 10;
        String[] catNames = {"All", "HUD", "Render", "Movement", "Player", "Utility"};
        ModuleCategory[] cats = {
                null,
                ModuleCategory.HUD,
                ModuleCategory.RENDER,
                ModuleCategory.MOVEMENT,
                ModuleCategory.PLAYER,
                ModuleCategory.UTILITY
        };

        for (int i = 0; i < catNames.length; i++) {
            int pw = this.textRenderer.getWidth(catNames[i]) + 14;
            if (RenderUtils.isHovered(mx, my, cursorX, barY, pw, pillH)) {
                selectedCategory = cats[i];
                scrollOffset = 0.0;
                searchFocused = false;
                return true;
            }
            cursorX += pw + 4;
        }

        int searchW = Math.max(76, Math.min(112, panelX + panelW - 10 - (cursorX + 98)));
        int searchX = panelX + panelW - 10 - searchW;
        if (RenderUtils.isHovered(mx, my, searchX, barY, searchW, pillH)) {
            searchFocused = true;
            if (button == 1) {
                searchQuery = "";
            }
            return true;
        }
        searchFocused = false;

        int presetsW = 46;
        int presetsX = searchX - presetsW - 4;
        if (RenderUtils.isHovered(mx, my, presetsX, barY, presetsW, pillH)) {
            modalState = ModalState.HUD_PRESETS;
            return true;
        }

        int resetW = 38;
        int resetX = presetsX - resetW - 4;
        if (resetX >= cursorX && RenderUtils.isHovered(mx, my, resetX, barY, resetW, pillH)) {
            modalState = ModalState.RESET_ALL;
            return true;
        }

        // Module cards
        List<Module> modules = ModuleManager.getInstance().getFiltered(selectedCategory, searchQuery);
        int gridX = panelX + 10;
        int gridY = panelY + 63;
        int gridW = panelW - 24;
        int gridH = panelH - 72;

        if (!RenderUtils.isHovered(mx, my, gridX, gridY, gridW, gridH)) {
            return super.mouseClicked(click, doubled);
        }

        int cols = gridW >= 420 ? 4 : (gridW >= 300 ? 3 : 2);
        int gap = 7;
        int cardW = (gridW - (cols - 1) * gap) / cols;
        int cardH = 84;

        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx = gridX + col * (cardW + gap);
            int cy = gridY + row * (cardH + gap) - (int) Math.round(scrollOffset);

            if (RenderUtils.isHovered(mx, my, cx, cy, cardW, cardH)) {
                int btnY = cy + cardH - 22;
                int gearX = cx + 6;
                if (button == 1 || RenderUtils.isHovered(mx, my, gearX, btnY, 18, 16)) {
                    activeSettingsModule = mod;
                    settingsScroll = 0.0;
                    editingStringSetting = null;
                    modalState = ModalState.MODULE_SETTINGS;
                    return true;
                }
                mod.toggle();
                FluxConfigManager.saveAsync();
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    private boolean handleOptimizerClick(double mx, double my) {
        LowEndOptimizer opt = LowEndOptimizer.getInstance();
        int x = panelX + 14;
        int y = panelY + 42;
        int w = panelW - 28;

        int profBtnW = 158;
        if (RenderUtils.isHovered(mx, my, x + w - profBtnW, y, profBtnW, 20)) {
            opt.applyLowEndVideoProfile(this.client);
            return true;
        }

        int rowY = y + 32;
        int rowH = 26;
        int step = rowH + 5;

        // Row 0: Entity Culling toggle
        if (RenderUtils.isHovered(mx, my, x + w - 64, rowY + 5, 56, 16)) {
            opt.setEntityCullingEnabled(!opt.isEntityCullingEnabled());
            FluxConfigManager.saveAsync();
            return true;
        }
        // Row 1: Passive Max Dist slider
        if (RenderUtils.isHovered(mx, my, x + w - 140, rowY + step, 134, rowH)) {
            draggingOptSlider = 1;
            updateOptimizerSlider(mx, x + w - 140, 130);
            return true;
        }
        // Row 2: Item Max Dist slider
        if (RenderUtils.isHovered(mx, my, x + w - 140, rowY + step * 2, 134, rowH)) {
            draggingOptSlider = 2;
            updateOptimizerSlider(mx, x + w - 140, 130);
            return true;
        }
        // Row 3: Particle Limiter toggle
        if (RenderUtils.isHovered(mx, my, x + w - 64, rowY + step * 3 + 5, 56, 16)) {
            opt.setParticleLimiterEnabled(!opt.isParticleLimiterEnabled());
            FluxConfigManager.saveAsync();
            return true;
        }
        // Row 4: Max Particles/Tick slider
        if (RenderUtils.isHovered(mx, my, x + w - 140, rowY + step * 4, 134, rowH)) {
            draggingOptSlider = 4;
            updateOptimizerSlider(mx, x + w - 140, 130);
            return true;
        }
        // Row 5: Particle Max Dist slider
        if (RenderUtils.isHovered(mx, my, x + w - 140, rowY + step * 5, 134, rowH)) {
            draggingOptSlider = 5;
            updateOptimizerSlider(mx, x + w - 140, 130);
            return true;
        }
        // Row 6: Unfocused FPS Limiter toggle
        if (RenderUtils.isHovered(mx, my, x + w - 64, rowY + step * 6 + 5, 56, 16)) {
            opt.setUnfocusedFpsLimiter(!opt.isUnfocusedFpsLimiter());
            FluxConfigManager.saveAsync();
            return true;
        }

        return false;
    }

    private void updateOptimizerSlider(double mouseX, int sliderX, int sliderW) {
        double ratio = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / (double) sliderW));
        LowEndOptimizer opt = LowEndOptimizer.getInstance();
        switch (draggingOptSlider) {
            case 1 -> opt.setPassiveEntityMaxDistance((int) Math.round(16 + ratio * (96 - 16)));
            case 2 -> opt.setItemEntityMaxDistance((int) Math.round(12 + ratio * (64 - 12)));
            case 4 -> opt.setMaxParticlesPerTick((int) Math.round(10 + ratio * (200 - 10)));
            case 5 -> opt.setParticleMaxDistance((int) Math.round(12 + ratio * (64 - 12)));
        }
    }

    private boolean handleModalClick(double mx, double my, int button) {
        if (modalState == ModalState.RESET_ALL) {
            int mw = 276;
            int mh = 104;
            int rx = (this.width - mw) / 2;
            int ry = (this.height - mh) / 2;
            if (RenderUtils.isHovered(mx, my, rx + mw - 26, ry + 7, 18, 18)
                    || !RenderUtils.isHovered(mx, my, rx, ry, mw, mh)) {
                modalState = ModalState.NONE;
                return true;
            }
            int cardW = (mw - 30) / 2;
            int cardH = 56;
            int cardY = ry + 34;
            if (RenderUtils.isHovered(mx, my, rx + 10, cardY, cardW, cardH)) {
                HudPresetManager.resetToDefaults();
                modalState = ModalState.NONE;
                return true;
            }
            if (RenderUtils.isHovered(mx, my, rx + 20 + cardW, cardY, cardW, cardH)) {
                HudPresetManager.disableAllModules();
                modalState = ModalState.NONE;
                return true;
            }
            return true;
        }

        if (modalState == ModalState.HUD_PRESETS) {
            int mw = Math.min(390, this.width - 28);
            int mh = 186;
            int px = (this.width - mw) / 2;
            int py = (this.height - mh) / 2;

            if (RenderUtils.isHovered(mx, my, px + mw - 26, py + 7, 18, 18)
                    || !RenderUtils.isHovered(mx, my, px, py, mw, mh)) {
                modalState = ModalState.NONE;
                return true;
            }

            int saveBtnW = 74;
            int saveBtnX = px + mw - 26 - saveBtnW - 6;
            if (RenderUtils.isHovered(mx, my, saveBtnX, py + 7, saveBtnW, 18)) {
                HudPresetManager.captureCurrentAsCustomLayout();
                FluxConfigManager.saveAsync();
                FluxNotificationOverlay.push("Custom Layout", "Saved current HUD layout to Slot 06", FluxNotificationOverlay.Type.SUCCESS);
                return true;
            }

            List<HudPresetManager.PresetInfo> presets = HudPresetManager.PRESETS;
            int gapX = 8;
            int gapY = 7;
            int cardW = (mw - 24 - gapX) / 2;
            int cardH = 42;
            int startY = py + 32;
            for (int i = 0; i < presets.size(); i++) {
                int col = i % 2;
                int row = i / 2;
                int cx = px + 12 + col * (cardW + gapX);
                int cy = startY + row * (cardH + gapY);
                if (RenderUtils.isHovered(mx, my, cx, cy, cardW, cardH)) {
                    HudPresetManager.applyPreset(presets.get(i).id());
                    modalState = ModalState.NONE;
                    return true;
                }
            }
            return true;
        }

        if (modalState == ModalState.MODULE_SETTINGS && activeSettingsModule != null) {
            int mw = Math.min(360, this.width - 30);
            int mh = Math.min(230, this.height - 30);
            int sx = (this.width - mw) / 2;
            int sy = (this.height - mh) / 2;

            editingStringSetting = null;

            if (RenderUtils.isHovered(mx, my, sx + mw - 26, sy + 7, 18, 18)
                    || !RenderUtils.isHovered(mx, my, sx, sy, mw, mh)) {
                modalState = ModalState.NONE;
                FluxConfigManager.saveAsync();
                return true;
            }

            if (activeSettingsModule instanceof HudModule && this.client != null) {
                int moveW = 58;
                int moveX = sx + mw - 26 - moveW - 6;
                if (RenderUtils.isHovered(mx, my, moveX, sy + 7, moveW, 18)) {
                    modalState = ModalState.NONE;
                    this.client.setScreen(new HudEditorScreen(this));
                    return true;
                }
            }

            List<Setting<?>> settings = activeSettingsModule.getSettings();
            int listX = sx + 10;
            int listY = sy + 34;
            int listW = mw - 20;
            int listH = mh - 44;
            int rowH = 24;
            int gap = 4;

            if (!RenderUtils.isHovered(mx, my, listX, listY, listW, listH)) {
                return true;
            }

            for (int i = 0; i < settings.size(); i++) {
                Setting<?> s = settings.get(i);
                int ry = listY + i * (rowH + gap) - (int) Math.round(settingsScroll);
                if (!RenderUtils.isHovered(mx, my, listX, ry, listW, rowH)) {
                    continue;
                }

                if (s instanceof BooleanSetting bs) {
                    bs.toggle();
                    FluxConfigManager.saveAsync();
                } else if (s instanceof ModeSetting ms) {
                    ms.cycleNext();
                    FluxConfigManager.saveAsync();
                } else if (s instanceof ColorSetting cs) {
                    cs.cycleNext();
                    FluxConfigManager.saveAsync();
                } else if (s instanceof NumberSetting ns) {
                    int sliderW = 96;
                    int sliderX = listX + listW - sliderW - 8;
                    draggingSlider = ns;
                    double ratio = Math.max(0.0, Math.min(1.0, (mx - sliderX) / (double) sliderW));
                    ns.setValue(ns.getMin() + ratio * (ns.getMax() - ns.getMin()));
                } else if (s instanceof StringSetting ss) {
                    editingStringSetting = ss;
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (draggingSlider != null && modalState == ModalState.MODULE_SETTINGS) {
            int mw = Math.min(360, this.width - 30);
            int sx = (this.width - mw) / 2;
            int listX = sx + 10;
            int listW = mw - 20;
            int sliderW = 96;
            int sliderX = listX + listW - sliderW - 8;
            double ratio = Math.max(0.0, Math.min(1.0, (click.x() - sliderX) / (double) sliderW));
            draggingSlider.setValue(draggingSlider.getMin() + ratio * (draggingSlider.getMax() - draggingSlider.getMin()));
            return true;
        }
        if (draggingOptSlider > 0 && optimizerTabActive) {
            int x = panelX + 14;
            int w = panelW - 28;
            updateOptimizerSlider(click.x(), x + w - 140, 130);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (draggingSlider != null || draggingOptSlider > 0) {
            draggingSlider = null;
            draggingOptSlider = -1;
            FluxConfigManager.saveAsync();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (modalState == ModalState.MODULE_SETTINGS) {
            settingsScroll -= verticalAmount * 18.0;
            return true;
        }
        if (modalState == ModalState.NONE && !optimizerTabActive) {
            scrollOffset = Math.max(0.0, Math.min(maxScroll, scrollOffset - verticalAmount * 22.0));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            if (modalState != ModalState.NONE) {
                modalState = ModalState.NONE;
                editingStringSetting = null;
                FluxConfigManager.saveAsync();
                return true;
            }
            if (searchFocused) {
                searchFocused = false;
                return true;
            }
            this.close();
            return true;
        }

        if (editingStringSetting != null) {
            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE) {
                String cur = editingStringSetting.getValue();
                if (!cur.isEmpty()) {
                    editingStringSetting.setValue(cur.substring(0, cur.length() - 1));
                }
                return true;
            }
            if (input.isEnter()) {
                editingStringSetting = null;
                FluxConfigManager.saveAsync();
                return true;
            }
            return true;
        }

        if (searchFocused) {
            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    scrollOffset = 0.0;
                }
                return true;
            }
            if (input.isEnter()) {
                searchFocused = false;
                return true;
            }
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!input.isValidChar()) {
            return false;
        }
        String ch = input.asString();
        if (editingStringSetting != null) {
            editingStringSetting.setValue(editingStringSetting.getValue() + ch);
            return true;
        }
        if (searchFocused && searchQuery.length() < 28) {
            searchQuery += ch;
            scrollOffset = 0.0;
            return true;
        }
        return super.charTyped(input);
    }
}
