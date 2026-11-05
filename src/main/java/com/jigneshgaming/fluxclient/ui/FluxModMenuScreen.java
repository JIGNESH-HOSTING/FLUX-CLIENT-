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
 * Features smooth animations, 4-column module cards, live search, per-module settings,
 * scrollable single-column HUD PRESETS modal (6 layouts), RESET ALL confirmation modal,
 * and a scrollable Low-End Optimizer panel tuned for 3 GB RAM Android / PojavLauncher & PC.
 */
public class FluxModMenuScreen extends Screen {
    public static final int ACCENT_ORANGE = 0xFFF05A24;
    public static final int ACCENT_ORANGE_HOVER = 0xFFFF6D38;
    public static final int ENABLED_GREEN = 0xFF1F7A38;
    public static final int ENABLED_GREEN_HOVER = 0xFF269645;
    public static final int PANEL_BG = 0xF50B0E12;
    public static final int CARD_BG = 0xFF0E1217;
    public static final int CARD_BORDER = 0xFF1C222B;

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

    private double targetScroll = 0.0;
    private double smoothScroll = 0.0;
    private double maxScroll = 0.0;

    private double optScroll = 0.0;
    private double maxOptScroll = 0.0;

    private double presetsScroll = 0.0;
    private double maxPresetsScroll = 0.0;

    private double settingsScroll = 0.0;

    // Zero-allocation smooth toggle/hover animation arrays (22 modules)
    private final float[] cardToggleAnim = new float[32];
    private final float[] cardHoverAnim = new float[32];
    private float modalAnim = 0.0f;

    private ModalState modalState = ModalState.NONE;
    private Module activeSettingsModule = null;
    private NumberSetting draggingSlider = null;
    private StringSetting editingStringSetting = null;
    private int draggingOptSlider = -1;

    private int navY;
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
        int totalAvailH = this.height - 28;
        int navBlockH = 28;
        this.panelW = Math.min(536, this.width - 18);
        this.panelH = Math.min(266, Math.max(170, totalAvailH - navBlockH));
        this.panelX = (this.width - this.panelW) / 2;
        int combinedH = navBlockH + this.panelH;
        this.navY = Math.max(6, (this.height - combinedH) / 2 - 2);
        this.panelY = this.navY + navBlockH;
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
        // Rendered inside render() without heavy FBO Gaussian blur passes.
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Fullscreen dark backdrop
        ctx.fill(0, 0, this.width, this.height, 0xA4030508);

        boolean modalOpen = modalState != ModalState.NONE;
        modalAnim = RenderUtils.animate(modalAnim, modalOpen, delta, 0.38f);

        int effMouseX = modalOpen ? -1000 : mouseX;
        int effMouseY = modalOpen ? -1000 : mouseY;

        // 1. Separated Top Navigation Bar matching Screenshots 093503-093602
        renderTopNavBar(ctx, effMouseX, effMouseY);

        // 2. Main Dark Module / Optimizer Container
        RenderUtils.drawRoundedBox(ctx, panelX, panelY, panelW, panelH, PANEL_BG, 0xFF1A2029);

        if (optimizerTabActive) {
            renderOptimizerPanel(ctx, effMouseX, effMouseY);
        } else {
            renderCategoryFilterBar(ctx, effMouseX, effMouseY);
            renderModuleGrid(ctx, effMouseX, effMouseY, delta);
        }

        // 3. Bottom-Center Version Label
        if (panelY + panelH + 11 <= this.height) {
            ctx.drawCenteredTextWithShadow(
                    this.textRenderer,
                    "Flux Client 1.21.11",
                    this.width / 2,
                    this.height - 10,
                    0xFF7D8895
            );
        }

        // 4. Active Modal Overlay
        if (modalOpen) {
            int dimAlpha = Math.round(0xB4 * Math.max(0.35f, modalAnim));
            ctx.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, (dimAlpha << 24) | 0x040609);
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

    private void renderTopNavBar(DrawContext ctx, int mouseX, int mouseY) {
        int tabH = 23;
        int modTabX = panelX;
        int modTabW = 86;

        boolean modTabHov = RenderUtils.isHovered(mouseX, mouseY, modTabX, navY, modTabW, tabH);
        int modTabFill = !optimizerTabActive
                ? (modTabHov ? ACCENT_ORANGE_HOVER : ACCENT_ORANGE)
                : (modTabHov ? 0xFF1D232C : 0xFF14181F);
        int modTabBorder = !optimizerTabActive ? ACCENT_ORANGE_HOVER : 0xFF262E3A;
        RenderUtils.drawRoundedBox(ctx, modTabX, navY, modTabW, tabH, modTabFill, modTabBorder);
        RenderUtils.drawFourSquareIcon(ctx, modTabX + 13, navY + 11, 0xFFFFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, "MOD MENU", modTabX + 23, navY + 8, 0xFFFFFFFF);

        // 6 top icon tabs: Settings, HUD Editor, Cosmetics, Low-End Optimizer, Quick Messages, HUD Presets
        int iconStartX = modTabX + modTabW + 6;
        int iconBtnW = 26;
        for (int i = 0; i < 6; i++) {
            int bx = iconStartX + i * (iconBtnW + 5);
            boolean active = (i == 3 && optimizerTabActive) || (i == 5 && modalState == ModalState.HUD_PRESETS);
            boolean hov = RenderUtils.isHovered(mouseX, mouseY, bx, navY, iconBtnW, tabH);
            int fill = active ? ACCENT_ORANGE : (hov ? 0xFF1E252F : 0xFF14181F);
            int border = active ? ACCENT_ORANGE_HOVER : (hov ? ACCENT_ORANGE : 0xFF242C37);
            RenderUtils.drawRoundedBox(ctx, bx, navY, iconBtnW, tabH, fill, border);

            int ic = (active || hov) ? 0xFFFFFFFF : 0xFFB8C2CE;
            int icx = bx + iconBtnW / 2;
            int icy = navY + 11;
            switch (i) {
                case 0 -> RenderUtils.drawGearIcon(ctx, icx, icy, ic);
                case 1 -> RenderUtils.drawMonitorIcon(ctx, icx, icy, ic);
                case 2 -> RenderUtils.drawHangerIcon(ctx, icx, icy, ic);
                case 3 -> RenderUtils.drawGaugeIcon(ctx, icx, icy, ic);
                case 4 -> RenderUtils.drawChatIcon(ctx, icx, icy, ic);
                case 5 -> RenderUtils.drawPersonGearIcon(ctx, icx, icy, ic);
            }
        }

        // Far-right orange-bordered [x] close button matching screenshots
        int closeW = 24;
        int closeX = panelX + panelW - closeW;
        boolean closeHov = RenderUtils.isHovered(mouseX, mouseY, closeX, navY, closeW, tabH);
        RenderUtils.drawRoundedBox(
                ctx, closeX, navY, closeW, tabH,
                closeHov ? ACCENT_ORANGE : 0xFF201310,
                ACCENT_ORANGE
        );
        ctx.drawCenteredTextWithShadow(
                this.textRenderer,
                "x",
                closeX + closeW / 2,
                navY + 7,
                closeHov ? 0xFFFFFFFF : ACCENT_ORANGE
        );
    }

    private void renderCategoryFilterBar(DrawContext ctx, int mouseX, int mouseY) {
        int barY = panelY + 9;
        int pillH = 19;
        int cursorX = panelX + 12;

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
            int fill = selected ? ACCENT_ORANGE : (hov ? 0xFF1B212A : 0xFF12161D);
            int border = selected ? ACCENT_ORANGE_HOVER : (hov ? 0xFF323C4A : 0xFF1E252F);
            RenderUtils.drawRoundedBox(ctx, cursorX, barY, pw, pillH, fill, border);
            ctx.drawCenteredTextWithShadow(
                    this.textRenderer,
                    catNames[i],
                    cursorX + pw / 2,
                    barY + 6,
                    selected ? 0xFFFFFFFF : 0xFFC2CCD6
            );
            cursorX += pw + 4;
        }

        // Right-aligned Search box (with magnifier icon), Presets button, and Reset button
        int searchW = Math.max(76, Math.min(106, panelX + panelW - 12 - (cursorX + 100)));
        int searchX = panelX + panelW - 12 - searchW;
        boolean searchHov = RenderUtils.isHovered(mouseX, mouseY, searchX, barY, searchW, pillH);
        RenderUtils.drawRoundedBox(
                ctx, searchX, barY, searchW, pillH,
                0xFF10141B,
                searchFocused ? ACCENT_ORANGE : (searchHov ? 0xFF384454 : 0xFF232B36)
        );
        RenderUtils.drawSearchMagnifierIcon(ctx, searchX + 9, barY + 10, searchFocused ? ACCENT_ORANGE : 0xFF7D8896);

        String displaySearch = searchQuery.isEmpty()
                ? (searchFocused ? "|" : "Search")
                : RenderUtils.ellipsize(this.textRenderer, searchQuery + (searchFocused ? "|" : ""), searchW - 22);
        int searchColor = searchQuery.isEmpty() && !searchFocused ? 0xFF8A95A3 : 0xFFF2F6FA;
        ctx.drawTextWithShadow(this.textRenderer, displaySearch, searchX + 17, barY + 6, searchColor);

        int presetsW = 50;
        int presetsX = searchX - presetsW - 5;
        boolean presetsActive = (modalState == ModalState.HUD_PRESETS);
        boolean presetsHov = RenderUtils.isHovered(mouseX, mouseY, presetsX, barY, presetsW, pillH);
        RenderUtils.drawRoundedBox(
                ctx, presetsX, barY, presetsW, pillH,
                presetsActive ? ACCENT_ORANGE : (presetsHov ? 0xFF1E252F : 0xFF13171E),
                presetsActive ? ACCENT_ORANGE_HOVER : (presetsHov ? ACCENT_ORANGE : 0xFF222A35)
        );
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Presets", presetsX + presetsW / 2, barY + 6, 0xFFF2F6FA);

        int resetW = 42;
        int resetX = presetsX - resetW - 5;
        if (resetX >= cursorX) {
            boolean resetActive = (modalState == ModalState.RESET_ALL);
            boolean resetHov = RenderUtils.isHovered(mouseX, mouseY, resetX, barY, resetW, pillH);
            RenderUtils.drawRoundedBox(
                    ctx, resetX, barY, resetW, pillH,
                    resetActive ? ACCENT_ORANGE : (resetHov ? 0xFF1E252F : 0xFF13171E),
                    resetActive ? ACCENT_ORANGE_HOVER : (resetHov ? ACCENT_ORANGE : 0xFF222A35)
            );
            ctx.drawCenteredTextWithShadow(this.textRenderer, "Reset", resetX + resetW / 2, barY + 6, 0xFFF2F6FA);
        }
    }

    private void renderModuleGrid(DrawContext ctx, int mouseX, int mouseY, float delta) {
        List<Module> modules = ModuleManager.getInstance().getFiltered(selectedCategory, searchQuery);

        int gridX = panelX + 12;
        int gridY = panelY + 35;
        int gridW = panelW - 28;
        int gridH = panelH - 43;

        int cols = gridW >= 410 ? 4 : (gridW >= 290 ? 3 : 2);
        int gap = 8;
        int cardW = (gridW - (cols - 1) * gap) / cols;
        int cardH = 88;

        int rows = (modules.size() + cols - 1) / cols;
        int contentH = Math.max(0, rows * cardH + Math.max(0, rows - 1) * gap);
        this.maxScroll = Math.max(0.0, contentH - gridH);
        this.targetScroll = Math.max(0.0, Math.min(this.maxScroll, this.targetScroll));
        this.smoothScroll += (this.targetScroll - this.smoothScroll) * Math.min(1.0, Math.max(0.15, delta * 0.42));
        if (Math.abs(this.targetScroll - this.smoothScroll) < 0.25) {
            this.smoothScroll = this.targetScroll;
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

        List<Module> allModules = ModuleManager.getInstance().getAllModules();

        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            int animIdx = Math.max(0, Math.min(cardToggleAnim.length - 1, allModules.indexOf(mod)));

            int col = i % cols;
            int row = i / cols;
            int cx = gridX + col * (cardW + gap);
            int cy = gridY + row * (cardH + gap) - (int) Math.round(smoothScroll);

            boolean cardHov = RenderUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH)
                    && RenderUtils.isHovered(mouseX, mouseY, gridX, gridY, gridW, gridH);

            cardToggleAnim[animIdx] = RenderUtils.animate(cardToggleAnim[animIdx], mod.isEnabled(), delta, 0.34f);
            cardHoverAnim[animIdx] = RenderUtils.animate(cardHoverAnim[animIdx], cardHov, delta, 0.34f);

            if (cy + cardH < gridY || cy > gridY + gridH) {
                continue;
            }

            int borderCol = RenderUtils.lerpArgb(CARD_BORDER, 0xFF313D4D, cardHoverAnim[animIdx]);
            RenderUtils.drawRoundedBox(ctx, cx, cy, cardW, cardH, CARD_BG, borderCol);

            // Top orange accent border strip when enabled (smoothly animated)
            if (cardToggleAnim[animIdx] > 0.02f) {
                int barAlpha = Math.round(255 * cardToggleAnim[animIdx]);
                ctx.fill(cx + 1, cy, cx + cardW - 1, cy + 2, (barAlpha << 24) | (ACCENT_ORANGE & 0x00FFFFFF));
            }

            // Left-aligned Module Name matching reference screenshots
            String title = RenderUtils.ellipsize(this.textRenderer, mod.getName(), cardW - 14);
            ctx.drawTextWithShadow(this.textRenderer, title, cx + 8, cy + 9, 0xFFF2F6FA);

            // Centered Custom Module Icon
            int iconColor = RenderUtils.lerpArgb(0xFFB5BFC9, 0xFFFFFFFF, Math.max(cardToggleAnim[animIdx], cardHoverAnim[animIdx] * 0.6f));
            RenderUtils.drawModuleIcon(ctx, this.textRenderer, mod.getIcon(), cx + cardW / 2, cy + 40, iconColor);

            // Bottom Action Row: [Gear] + [Enabled / Disabled]
            int btnY = cy + cardH - 24;
            int gearX = cx + 7;
            int gearW = 19;
            int btnH = 17;
            boolean gearHov = cardHov && RenderUtils.isHovered(mouseX, mouseY, gearX, btnY, gearW, btnH);
            RenderUtils.drawRoundedBox(
                    ctx, gearX, btnY, gearW, btnH,
                    gearHov ? 0xFF232B36 : 0xFF151920,
                    gearHov ? ACCENT_ORANGE : 0xFF232A35
            );
            RenderUtils.drawGearIcon(ctx, gearX + gearW / 2, btnY + btnH / 2, gearHov ? 0xFFFFFFFF : 0xFFA6B1BE);

            int togX = gearX + gearW + 5;
            int togW = cardW - 14 - gearW - 5;
            boolean togHov = cardHov && RenderUtils.isHovered(mouseX, mouseY, togX, btnY, togW, btnH);
            int baseTogFill = RenderUtils.lerpArgb(0xFF151920, ENABLED_GREEN, cardToggleAnim[animIdx]);
            if (togHov) {
                baseTogFill = mod.isEnabled() ? ENABLED_GREEN_HOVER : 0xFF1F2630;
            }
            int togBorder = RenderUtils.lerpArgb(0xFF232A35, 0xFF2CA852, cardToggleAnim[animIdx]);
            RenderUtils.drawRoundedBox(ctx, togX, btnY, togW, btnH, baseTogFill, togBorder);

            int labelCol = RenderUtils.lerpArgb(0xFFC0CAD5, 0xFF7BFF9F, cardToggleAnim[animIdx]);
            ctx.drawCenteredTextWithShadow(
                    this.textRenderer,
                    mod.isEnabled() ? "Enabled" : "Disabled",
                    togX + togW / 2,
                    btnY + 5,
                    mod.isEnabled() ? 0xFF98FFB4 : labelCol
            );
        }

        ctx.disableScissor();

        // Right-side vertical scrollbar matching screenshots
        if (this.maxScroll > 0.0) {
            int trackX = panelX + panelW - 9;
            ctx.fill(trackX, gridY, trackX + 3, gridY + gridH, 0xFF151A22);
            int thumbH = Math.max(22, (int) ((double) gridH * gridH / (double) contentH));
            int thumbY = gridY + (int) ((smoothScroll / maxScroll) * (gridH - thumbH));
            ctx.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFFE8432E);
        }
    }

    private void renderOptimizerPanel(DrawContext ctx, int mouseX, int mouseY) {
        LowEndOptimizer opt = LowEndOptimizer.getInstance();
        int x = panelX + 14;
        int y = panelY + 10;
        int w = panelW - 28;

        ctx.drawTextWithShadow(this.textRenderer, "LOW-END & POJAVLAUNCHER OPTIMIZER", x, y + 2, ACCENT_ORANGE);
        ctx.drawTextWithShadow(
                this.textRenderer,
                "Tuned for 3 GB RAM Android (Vivo 1916 / PojavLauncher) & low-end PCs",
                x, y + 13, 0xFF8E9BA8
        );

        int profBtnW = 148;
        int profBtnX = x + w - profBtnW;
        boolean profHov = RenderUtils.isHovered(mouseX, mouseY, profBtnX, y + 2, profBtnW, 19);
        RenderUtils.drawRoundedBox(
                ctx, profBtnX, y + 2, profBtnW, 19,
                profHov ? ACCENT_ORANGE_HOVER : ACCENT_ORANGE,
                ACCENT_ORANGE_HOVER
        );
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Apply Low-End Profile", profBtnX + profBtnW / 2, y + 8, 0xFFFFFFFF);

        int listY = y + 28;
        int listH = panelH - 38;
        int rowH = 25;
        int gap = 4;
        int totalRows = 10;
        int totalContentH = totalRows * (rowH + gap);
        this.maxOptScroll = Math.max(0.0, totalContentH - listH);
        this.optScroll = Math.max(0.0, Math.min(this.maxOptScroll, this.optScroll));

        ctx.enableScissor(x, listY, x + w, listY + listH);
        int curY = listY - (int) Math.round(optScroll);
        int step = rowH + gap;

        drawOptimizerToggleRow(ctx, x, curY, w, rowH, "Entity Distance Culling",
                "Skips distant passive/ambient/item entities while keeping players & bosses visible",
                opt.isEntityCullingEnabled(), mouseX, mouseY, listY, listY + listH);
        curY += step;

        drawOptimizerSliderRow(ctx, x, curY, w, rowH, "Passive Entity Max Distance",
                opt.getPassiveEntityMaxDistance() + " blocks",
                (opt.getPassiveEntityMaxDistance() - 16.0) / (96.0 - 16.0));
        curY += step;

        drawOptimizerSliderRow(ctx, x, curY, w, rowH, "Dropped Item / XP Max Distance",
                opt.getItemEntityMaxDistance() + " blocks",
                (opt.getItemEntityMaxDistance() - 12.0) / (64.0 - 12.0));
        curY += step;

        drawOptimizerToggleRow(ctx, x, curY, w, rowH, "Block Entity Distance Culling",
                "Skips distant chests, signs, banners & skulls (keeps beacons & portals visible)",
                opt.isBlockEntityCullingEnabled(), mouseX, mouseY, listY, listY + listH);
        curY += step;

        drawOptimizerSliderRow(ctx, x, curY, w, rowH, "Block Entity Max Distance",
                opt.getBlockEntityMaxDistance() + " blocks",
                (opt.getBlockEntityMaxDistance() - 16.0) / (64.0 - 16.0));
        curY += step;

        drawOptimizerToggleRow(ctx, x, curY, w, rowH, "Particle Burst & Distance Limiter",
                "Prevents explosion, crystal & weather particle floods from stalling mobile GPUs",
                opt.isParticleLimiterEnabled(), mouseX, mouseY, listY, listY + listH);
        curY += step;

        drawOptimizerSliderRow(ctx, x, curY, w, rowH, "Max New Particles Per Tick",
                opt.getMaxParticlesPerTick() + " / tick",
                (opt.getMaxParticlesPerTick() - 10.0) / (200.0 - 10.0));
        curY += step;

        drawOptimizerSliderRow(ctx, x, curY, w, rowH, "Particle Max Render Distance",
                opt.getParticleMaxDistance() + " blocks",
                (opt.getParticleMaxDistance() - 12.0) / (64.0 - 12.0));
        curY += step;

        drawOptimizerToggleRow(ctx, x, curY, w, rowH, "Skip Heavy Menu Gaussian Blur",
                "Eliminates multi-pass menu FBO blur lag on PojavLauncher / GL4ES / mobile GPUs",
                opt.isMenuBlurBypass(), mouseX, mouseY, listY, listY + listH);
        curY += step;

        drawOptimizerToggleRow(ctx, x, curY, w, rowH, "Unfocused Window FPS Limiter",
                "Caps background frame rate (" + opt.getUnfocusedMaxFps() + " FPS) to save battery & reduce heat",
                opt.isUnfocusedFpsLimiter(), mouseX, mouseY, listY, listY + listH);

        ctx.disableScissor();
    }

    private void drawOptimizerToggleRow(DrawContext ctx, int x, int y, int w, int h,
                                        String title, String subtitle, boolean enabled,
                                        int mouseX, int mouseY, int clipTop, int clipBottom) {
        if (y + h < clipTop || y > clipBottom) return;
        RenderUtils.drawRoundedBox(ctx, x, y, w, h, CARD_BG, CARD_BORDER);
        ctx.drawTextWithShadow(this.textRenderer, title, x + 8, y + 4, 0xFFF0F5FA);
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(x + 8, y + 15);
        ctx.getMatrices().scale(0.78f, 0.78f);
        ctx.drawTextWithShadow(this.textRenderer, RenderUtils.ellipsize(this.textRenderer, subtitle, (int) ((w - 84) / 0.78f)), 0, 0, 0xFF8692A0);
        ctx.getMatrices().popMatrix();

        int bx = x + w - 64;
        int by = y + 4;
        boolean hov = mouseY >= clipTop && mouseY <= clipBottom && RenderUtils.isHovered(mouseX, mouseY, bx, by, 56, 17);
        RenderUtils.drawRoundedBox(
                ctx, bx, by, 56, 17,
                enabled ? (hov ? ENABLED_GREEN_HOVER : ENABLED_GREEN) : (hov ? 0xFF222A34 : 0xFF151A21),
                enabled ? 0xFF2CA852 : 0xFF262E3A
        );
        ctx.drawCenteredTextWithShadow(this.textRenderer, enabled ? "Enabled" : "Disabled", bx + 28, by + 5, enabled ? 0xFF98FFB4 : 0xFFC2CCD6);
    }

    private void drawOptimizerSliderRow(DrawContext ctx, int x, int y, int w, int h,
                                        String title, String valLabel, double ratio) {
        RenderUtils.drawRoundedBox(ctx, x, y, w, h, CARD_BG, CARD_BORDER);
        ctx.drawTextWithShadow(this.textRenderer, title, x + 8, y + 8, 0xFFE6EEF6);

        int sliderW = 126;
        int sliderX = x + w - sliderW - 10;
        int sliderY = y + 12;
        ctx.drawTextWithShadow(this.textRenderer, valLabel, sliderX - this.textRenderer.getWidth(valLabel) - 8, y + 8, ACCENT_ORANGE);

        ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 4, 0xFF202732);
        int fillW = (int) Math.round(Math.max(0.0, Math.min(1.0, ratio)) * sliderW);
        ctx.fill(sliderX, sliderY, sliderX + fillW, sliderY + 4, ACCENT_ORANGE);
        ctx.fill(sliderX + fillW - 2, sliderY - 2, sliderX + fillW + 3, sliderY + 6, 0xFFFFFFFF);
    }

    // ==================== MODALS ====================

    private void renderResetModal(DrawContext ctx, int mouseX, int mouseY) {
        int mw = Math.min(316, this.width - 28);
        int mh = 100;
        int mx = (this.width - mw) / 2;
        int my = (this.height - mh) / 2;

        RenderUtils.drawRoundedBox(ctx, mx, my, mw, mh, 0xFC0C1015, 0xFF242D39);
        ctx.drawTextWithShadow(this.textRenderer, "RESET ALL", mx + 12, my + 11, 0xFFF4F8FC);
        ctx.drawTextWithShadow(
                this.textRenderer,
                RenderUtils.ellipsize(this.textRenderer, "Choose how much of your HUD setup should be reset.", mw - 38),
                mx + 12, my + 24, 0xFF8894A2
        );

        int closeX = mx + mw - 24;
        int closeY = my + 8;
        boolean closeHov = RenderUtils.isHovered(mouseX, mouseY, closeX, closeY, 16, 16);
        RenderUtils.drawRoundedBox(ctx, closeX, closeY, 16, 16, closeHov ? ACCENT_ORANGE : 0xFF151A22, closeHov ? ACCENT_ORANGE_HOVER : 0xFF262F3C);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "x", closeX + 8, closeY + 4, 0xFFDCE6F2);

        int cardW = (mw - 32) / 2;
        int cardH = 42;
        int cardY = my + 44;

        // Left card: DEFAULTS — Restore original setup
        int defX = mx + 12;
        boolean defHov = RenderUtils.isHovered(mouseX, mouseY, defX, cardY, cardW, cardH);
        RenderUtils.drawRoundedBox(
                ctx, defX, cardY, cardW, cardH,
                defHov ? 0xFF14211B : 0xFF11161C,
                defHov ? 0xFF2ECC71 : 0xFF202A34
        );
        ctx.drawTextWithShadow(this.textRenderer, "DEFAULTS", defX + 9, cardY + 9, 0xFFF4F8FC);
        ctx.drawTextWithShadow(
                this.textRenderer,
                RenderUtils.ellipsize(this.textRenderer, "Restore original setup", cardW - 16),
                defX + 9, cardY + 23, 0xFF8C98A6
        );

        // Right card: REMOVE ALL — Disable every module
        int remX = defX + cardW + 8;
        boolean remHov = RenderUtils.isHovered(mouseX, mouseY, remX, cardY, cardW, cardH);
        RenderUtils.drawRoundedBox(
                ctx, remX, cardY, cardW, cardH,
                remHov ? 0xFF241417 : 0xFF13161C,
                remHov ? 0xFFFF4D58 : 0xFF242D38
        );
        ctx.drawTextWithShadow(this.textRenderer, "REMOVE ALL", remX + 9, cardY + 9, 0xFFF4F8FC);
        ctx.drawTextWithShadow(
                this.textRenderer,
                RenderUtils.ellipsize(this.textRenderer, "Disable every module", cardW - 16),
                remX + 9, cardY + 23, 0xFFDCE4EE
        );
    }

    /**
     * Scrollable single-column vertical HUD PRESETS modal matching Screenshot_20261105_093602.jpg.
     */
    private void renderPresetsModal(DrawContext ctx, int mouseX, int mouseY) {
        int mw = Math.min(346, this.width - 28);
        int mh = Math.min(220, this.height - 22);
        int mx = (this.width - mw) / 2;
        int my = (this.height - mh) / 2;

        RenderUtils.drawRoundedBox(ctx, mx, my, mw, mh, 0xFC0B0F14, 0xFF242E3B);
        ctx.drawTextWithShadow(this.textRenderer, "HUD PRESETS", mx + 12, my + 10, 0xFFF4F8FC);

        String layoutsCount = "6 layouts";
        int countW = this.textRenderer.getWidth(layoutsCount);
        ctx.drawTextWithShadow(this.textRenderer, layoutsCount, mx + mw - 12 - countW, my + 23, 0xFF8894A2);
        ctx.drawTextWithShadow(
                this.textRenderer,
                RenderUtils.ellipsize(this.textRenderer, "Apply a full-screen layout tuned for a specific playstyle.", mw - 30 - countW),
                mx + 12, my + 23, 0xFF8894A2
        );

        int closeX = mx + mw - 24;
        int closeY = my + 7;
        boolean closeHov = RenderUtils.isHovered(mouseX, mouseY, closeX, closeY, 16, 16);
        RenderUtils.drawRoundedBox(ctx, closeX, closeY, 16, 16, closeHov ? ACCENT_ORANGE : 0xFF151A22, closeHov ? ACCENT_ORANGE_HOVER : 0xFF262F3C);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "x", closeX + 8, closeY + 4, 0xFFDCE6F2);

        List<HudPresetManager.PresetInfo> presets = HudPresetManager.PRESETS;
        int listX = mx + 10;
        int listY = my + 37;
        int listW = mw - 20;
        int listH = mh - 45;
        int rowH = 34;
        int gap = 5;

        int totalContentH = presets.size() * rowH + (presets.size() - 1) * gap;
        this.maxPresetsScroll = Math.max(0.0, totalContentH - listH);
        this.presetsScroll = Math.max(0.0, Math.min(this.maxPresetsScroll, this.presetsScroll));

        ctx.enableScissor(listX, listY, listX + listW, listY + listH);

        for (int i = 0; i < presets.size(); i++) {
            HudPresetManager.PresetInfo p = presets.get(i);
            int ry = listY + i * (rowH + gap) - (int) Math.round(presetsScroll);
            if (ry + rowH < listY || ry > listY + listH) continue;

            boolean rowHov = RenderUtils.isHovered(mouseX, mouseY, listX, ry, listW, rowH)
                    && RenderUtils.isHovered(mouseX, mouseY, listX, listY, listW, listH);
            RenderUtils.drawRoundedBox(ctx, listX, ry, listW, rowH, 0xFF11161E, rowHov ? 0xFF334050 : 0xFF1F2733);

            // Colored 3px left vertical accent bar matching Screenshot 093602
            ctx.fill(listX, ry + 1, listX + 3, ry + rowH - 1, p.accentColor());

            // Colored 01..06 code on the left
            ctx.drawTextWithShadow(this.textRenderer, p.code(), listX + 10, ry + 13, p.accentColor());

            boolean isCustom = "custom_layout".equals(p.id());
            int applyW = 46;
            int applyH = 18;
            int applyX = listX + listW - applyW - 7;
            int applyY = ry + 8;

            int saveW = isCustom ? 40 : 0;
            int saveX = applyX - saveW - 4;
            int maxTextW = (isCustom ? saveX : applyX) - (listX + 32) - 8;

            ctx.drawTextWithShadow(this.textRenderer, p.name(), listX + 32, ry + 7, 0xFFF2F6FA);
            ctx.drawTextWithShadow(
                    this.textRenderer,
                    RenderUtils.ellipsize(this.textRenderer, p.subtitle(), maxTextW),
                    listX + 32, ry + 19, 0xFF83909E
            );

            if (isCustom) {
                boolean saveHov = rowHov && RenderUtils.isHovered(mouseX, mouseY, saveX, applyY, saveW, applyH);
                RenderUtils.drawRoundedBox(ctx, saveX, applyY, saveW, applyH, saveHov ? 0xFF222D3A : 0xFF171E27, saveHov ? p.accentColor() : 0xFF2B3544);
                ctx.drawCenteredTextWithShadow(this.textRenderer, "SAVE", saveX + saveW / 2, applyY + 5, 0xFFDCE6F2);
            }

            boolean applyHov = rowHov && RenderUtils.isHovered(mouseX, mouseY, applyX, applyY, applyW, applyH);
            RenderUtils.drawRoundedBox(
                    ctx, applyX, applyY, applyW, applyH,
                    applyHov ? ACCENT_ORANGE : 0xFF19202A,
                    applyHov ? ACCENT_ORANGE_HOVER : 0xFF2B3544
            );
            ctx.drawCenteredTextWithShadow(this.textRenderer, "APPLY", applyX + applyW / 2, applyY + 5, 0xFFE8F0F8);
        }

        ctx.disableScissor();
    }

    private void renderModuleSettingsModal(DrawContext ctx, int mouseX, int mouseY) {
        if (activeSettingsModule == null) {
            modalState = ModalState.NONE;
            return;
        }
        int mw = Math.min(360, this.width - 28);
        int mh = Math.min(224, this.height - 24);
        int mx = (this.width - mw) / 2;
        int my = (this.height - mh) / 2;

        RenderUtils.drawRoundedBox(ctx, mx, my, mw, mh, 0xFC0C1016, 0xFF2A3442);
        ctx.drawTextWithShadow(this.textRenderer, activeSettingsModule.getName().toUpperCase() + " SETTINGS", mx + 12, my + 10, ACCENT_ORANGE);
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(mx + 12, my + 21);
        ctx.getMatrices().scale(0.8f, 0.8f);
        ctx.drawTextWithShadow(this.textRenderer, RenderUtils.ellipsize(this.textRenderer, activeSettingsModule.getDescription(), (int) ((mw - 105) / 0.8f)), 0, 0, 0xFF8E9BA8);
        ctx.getMatrices().popMatrix();

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
        settingsScroll = Math.max(0.0, Math.min(maxSetScroll, settingsScroll));

        ctx.enableScissor(listX, listY, listX + listW, listY + listH);

        for (int i = 0; i < settings.size(); i++) {
            Setting<?> s = settings.get(i);
            int ry = listY + i * (rowH + gap) - (int) Math.round(settingsScroll);
            if (ry + rowH < listY || ry > listY + listH) continue;

            RenderUtils.drawRoundedBox(ctx, listX, ry, listW, rowH, 0xFF131820, 0xFF232B37);
            ctx.drawTextWithShadow(this.textRenderer, s.getName(), listX + 8, ry + 8, 0xFFE6EEF6);

            if (s instanceof BooleanSetting bs) {
                int bx = listX + listW - 54;
                int by = ry + 4;
                boolean hov = RenderUtils.isHovered(mouseX, mouseY, bx, by, 46, 16);
                RenderUtils.drawRoundedBox(
                        ctx, bx, by, 46, 16,
                        bs.getValue() ? (hov ? ENABLED_GREEN_HOVER : ENABLED_GREEN) : (hov ? 0xFF252E3A : 0xFF191F28),
                        bs.getValue() ? 0xFF2CA852 : 0xFF2C3543
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

        // 1. Top Nav Bar
        int tabH = 23;
        int modTabX = panelX;
        int modTabW = 86;
        if (RenderUtils.isHovered(mx, my, modTabX, navY, modTabW, tabH)) {
            optimizerTabActive = false;
            return true;
        }

        int iconStartX = modTabX + modTabW + 6;
        int iconBtnW = 26;
        for (int i = 0; i < 6; i++) {
            int bx = iconStartX + i * (iconBtnW + 5);
            if (RenderUtils.isHovered(mx, my, bx, navY, iconBtnW, tabH)) {
                if (this.client == null) return true;
                switch (i) {
                    case 0 -> this.client.setScreen(new OptionsScreen(this, this.client.options));
                    case 1 -> this.client.setScreen(new HudEditorScreen(this));
                    case 2 -> this.client.setScreen(new FluxCosmeticsScreen(this));
                    case 3 -> optimizerTabActive = !optimizerTabActive;
                    case 4 -> this.client.setScreen(new QuickMessagesScreen(this));
                    case 5 -> {
                        presetsScroll = 0.0;
                        modalState = ModalState.HUD_PRESETS;
                    }
                }
                return true;
            }
        }

        int closeW = 24;
        if (RenderUtils.isHovered(mx, my, panelX + panelW - closeW, navY, closeW, tabH)) {
            this.close();
            return true;
        }

        if (optimizerTabActive) {
            return handleOptimizerClick(mx, my);
        }

        // 2. Category Bar & Search / Presets / Reset
        int barY = panelY + 9;
        int pillH = 19;
        int cursorX = panelX + 12;
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
                targetScroll = 0.0;
                smoothScroll = 0.0;
                searchFocused = false;
                return true;
            }
            cursorX += pw + 4;
        }

        int searchW = Math.max(76, Math.min(106, panelX + panelW - 12 - (cursorX + 100)));
        int searchX = panelX + panelW - 12 - searchW;
        if (RenderUtils.isHovered(mx, my, searchX, barY, searchW, pillH)) {
            searchFocused = true;
            if (button == 1) {
                searchQuery = "";
            }
            return true;
        }
        searchFocused = false;

        int presetsW = 50;
        int presetsX = searchX - presetsW - 5;
        if (RenderUtils.isHovered(mx, my, presetsX, barY, presetsW, pillH)) {
            presetsScroll = 0.0;
            modalState = ModalState.HUD_PRESETS;
            return true;
        }

        int resetW = 42;
        int resetX = presetsX - resetW - 5;
        if (resetX >= cursorX && RenderUtils.isHovered(mx, my, resetX, barY, resetW, pillH)) {
            modalState = ModalState.RESET_ALL;
            return true;
        }

        // 3. Module Cards
        List<Module> modules = ModuleManager.getInstance().getFiltered(selectedCategory, searchQuery);
        int gridX = panelX + 12;
        int gridY = panelY + 35;
        int gridW = panelW - 28;
        int gridH = panelH - 43;

        if (!RenderUtils.isHovered(mx, my, gridX, gridY, gridW, gridH)) {
            return super.mouseClicked(click, doubled);
        }

        int cols = gridW >= 410 ? 4 : (gridW >= 290 ? 3 : 2);
        int gap = 8;
        int cardW = (gridW - (cols - 1) * gap) / cols;
        int cardH = 88;

        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx = gridX + col * (cardW + gap);
            int cy = gridY + row * (cardH + gap) - (int) Math.round(smoothScroll);

            if (RenderUtils.isHovered(mx, my, cx, cy, cardW, cardH)) {
                int btnY = cy + cardH - 24;
                int gearX = cx + 7;
                if (button == 1 || RenderUtils.isHovered(mx, my, gearX, btnY, 19, 17)) {
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
        int y = panelY + 10;
        int w = panelW - 28;

        int profBtnW = 148;
        if (RenderUtils.isHovered(mx, my, x + w - profBtnW, y + 2, profBtnW, 19)) {
            opt.applyLowEndVideoProfile(this.client);
            return true;
        }

        int listY = y + 28;
        int listH = panelH - 38;
        if (!RenderUtils.isHovered(mx, my, x, listY, w, listH)) {
            return false;
        }

        int rowH = 25;
        int step = rowH + 4;
        int baseRowY = listY - (int) Math.round(optScroll);

        for (int row = 0; row < 10; row++) {
            int ry = baseRowY + row * step;
            if (!RenderUtils.isHovered(mx, my, x, ry, w, rowH)) {
                continue;
            }
            switch (row) {
                case 0 -> {
                    if (RenderUtils.isHovered(mx, my, x + w - 64, ry + 4, 56, 17)) {
                        opt.setEntityCullingEnabled(!opt.isEntityCullingEnabled());
                        FluxConfigManager.saveAsync();
                    }
                }
                case 3 -> {
                    if (RenderUtils.isHovered(mx, my, x + w - 64, ry + 4, 56, 17)) {
                        opt.setBlockEntityCullingEnabled(!opt.isBlockEntityCullingEnabled());
                        FluxConfigManager.saveAsync();
                    }
                }
                case 5 -> {
                    if (RenderUtils.isHovered(mx, my, x + w - 64, ry + 4, 56, 17)) {
                        opt.setParticleLimiterEnabled(!opt.isParticleLimiterEnabled());
                        FluxConfigManager.saveAsync();
                    }
                }
                case 8 -> {
                    if (RenderUtils.isHovered(mx, my, x + w - 64, ry + 4, 56, 17)) {
                        opt.setMenuBlurBypass(!opt.isMenuBlurBypass());
                        FluxConfigManager.saveAsync();
                    }
                }
                case 9 -> {
                    if (RenderUtils.isHovered(mx, my, x + w - 64, ry + 4, 56, 17)) {
                        opt.setUnfocusedFpsLimiter(!opt.isUnfocusedFpsLimiter());
                        FluxConfigManager.saveAsync();
                    }
                }
                case 1, 2, 4, 6, 7 -> {
                    int sliderW = 126;
                    int sliderX = x + w - sliderW - 10;
                    if (mx >= sliderX - 6 && mx <= sliderX + sliderW + 6) {
                        draggingOptSlider = row;
                        updateOptimizerSlider(mx, sliderX, sliderW);
                    }
                }
            }
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
            case 4 -> opt.setBlockEntityMaxDistance((int) Math.round(16 + ratio * (64 - 16)));
            case 6 -> opt.setMaxParticlesPerTick((int) Math.round(10 + ratio * (200 - 10)));
            case 7 -> opt.setParticleMaxDistance((int) Math.round(12 + ratio * (64 - 12)));
        }
    }

    private boolean handleModalClick(double mx, double my, int button) {
        if (modalState == ModalState.RESET_ALL) {
            int mw = Math.min(316, this.width - 28);
            int mh = 100;
            int rx = (this.width - mw) / 2;
            int ry = (this.height - mh) / 2;
            if (RenderUtils.isHovered(mx, my, rx + mw - 24, ry + 8, 16, 16)
                    || !RenderUtils.isHovered(mx, my, rx, ry, mw, mh)) {
                modalState = ModalState.NONE;
                return true;
            }
            int cardW = (mw - 32) / 2;
            int cardH = 42;
            int cardY = ry + 44;
            if (RenderUtils.isHovered(mx, my, rx + 12, cardY, cardW, cardH)) {
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
            int mw = Math.min(346, this.width - 28);
            int mh = Math.min(220, this.height - 22);
            int px = (this.width - mw) / 2;
            int py = (this.height - mh) / 2;

            if (RenderUtils.isHovered(mx, my, px + mw - 24, py + 7, 16, 16)
                    || !RenderUtils.isHovered(mx, my, px, py, mw, mh)) {
                modalState = ModalState.NONE;
                return true;
            }

            List<HudPresetManager.PresetInfo> presets = HudPresetManager.PRESETS;
            int listX = px + 10;
            int listY = py + 37;
            int listW = mw - 20;
            int listH = mh - 45;
            int rowH = 34;
            int gap = 5;

            if (!RenderUtils.isHovered(mx, my, listX, listY, listW, listH)) {
                return true;
            }

            for (int i = 0; i < presets.size(); i++) {
                HudPresetManager.PresetInfo p = presets.get(i);
                int ry = listY + i * (rowH + gap) - (int) Math.round(presetsScroll);
                if (!RenderUtils.isHovered(mx, my, listX, ry, listW, rowH)) {
                    continue;
                }
                boolean isCustom = "custom_layout".equals(p.id());
                int applyW = 46;
                int applyH = 18;
                int applyX = listX + listW - applyW - 7;
                int applyY = ry + 8;
                if (isCustom) {
                    int saveW = 40;
                    int saveX = applyX - saveW - 4;
                    if (RenderUtils.isHovered(mx, my, saveX, applyY, saveW, applyH)) {
                        HudPresetManager.saveCustomLayout(true);
                        return true;
                    }
                }
                HudPresetManager.applyPreset(p.id());
                modalState = ModalState.NONE;
                return true;
            }
            return true;
        }

        if (modalState == ModalState.MODULE_SETTINGS && activeSettingsModule != null) {
            int mw = Math.min(360, this.width - 28);
            int mh = Math.min(224, this.height - 24);
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
            int mw = Math.min(360, this.width - 28);
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
            int sliderW = 126;
            updateOptimizerSlider(click.x(), x + w - sliderW - 10, sliderW);
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
        if (modalState == ModalState.HUD_PRESETS) {
            presetsScroll = Math.max(0.0, Math.min(maxPresetsScroll, presetsScroll - verticalAmount * 20.0));
            return true;
        }
        if (modalState == ModalState.NONE && optimizerTabActive) {
            optScroll = Math.max(0.0, Math.min(maxOptScroll, optScroll - verticalAmount * 20.0));
            return true;
        }
        if (modalState == ModalState.NONE) {
            targetScroll = Math.max(0.0, Math.min(maxScroll, targetScroll - verticalAmount * 24.0));
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
                    targetScroll = 0.0;
                    smoothScroll = 0.0;
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
            targetScroll = 0.0;
            smoothScroll = 0.0;
            return true;
        }
        return super.charTyped(input);
    }
}
