package com.jigneshgaming.fluxclient.module.impl.render;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.ColorSetting;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;

public final class BlockOverlayModule extends Module {
    private final ColorSetting outlineColorSetting;
    private final NumberSetting lineWidthSetting;
    private final BooleanSetting fillHighlightSetting;
    private final NumberSetting fillOpacitySetting;

    public BlockOverlayModule() {
        super(
                "block_overlay",
                "Block Overlay",
                "Customizes targeted block wireframe outline color, thickness, and translucent fill.",
                ModuleCategory.RENDER,
                ModuleIcon.BLOCK_OVERLAY,
                false
        );
        this.outlineColorSetting = addSetting(new ColorSetting("outlineColor", "Outline Color", "Block outline color", "Flux Blue"));
        this.lineWidthSetting = addSetting(new NumberSetting("lineWidth", "Line Width", "Wireframe outline thickness", 2.5, 1.0, 5.0, 0.5, "px"));
        this.fillHighlightSetting = addSetting(new BooleanSetting("fillHighlight", "Filled Highlight", "Render subtle translucent fill on block faces", true));
        this.fillOpacitySetting = addSetting(new NumberSetting("fillOpacity", "Fill Opacity", "Translucent face highlight opacity", 45, 10, 140, 5, ""));
    }

    public boolean renderBlockOutline(WorldRenderContext context, OutlineRenderState outlineState) {
        if (!isEnabled() || outlineState == null || context.consumers() == null) {
            return false;
        }
        VoxelShape shape = outlineState.shape();
        if (shape == null || shape.isEmpty()) {
            return false;
        }

        Vec3d cam = context.worldState().cameraRenderState.pos;
        BlockPos pos = outlineState.pos();
        double ox = pos.getX() - cam.x;
        double oy = pos.getY() - cam.y;
        double oz = pos.getZ() - cam.z;

        MatrixStack matrices = context.matrices();
        int lineColor = outlineColorSetting.resolveArgb(245);
        float width = lineWidthSetting.getFloat();

        VertexRendering.drawOutline(
                matrices,
                context.consumers().getBuffer(RenderLayers.lines()),
                shape,
                ox,
                oy,
                oz,
                lineColor,
                width
        );

        if (fillHighlightSetting.get()) {
            int fillColor = outlineColorSetting.resolveArgb(fillOpacitySetting.getInt());
            VertexConsumer fillBuffer = context.consumers().getBuffer(RenderLayers.debugFilledBox());
            matrices.push();
            matrices.translate(ox, oy, oz);
            Matrix4f mat = matrices.peek().getPositionMatrix();
            shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) ->
                    drawFilledBox(mat, fillBuffer,
                            (float) (minX - 0.002), (float) (minY - 0.002), (float) (minZ - 0.002),
                            (float) (maxX + 0.002), (float) (maxY + 0.002), (float) (maxZ + 0.002),
                            fillColor)
            );
            matrices.pop();
        }

        return true;
    }

    /**
     * Emits 6 outward-facing CCW quads compatible with VIEW_OFFSET_Z_LAYERING & backface culling.
     */
    public static void drawFilledBox(
            Matrix4f mat,
            VertexConsumer vc,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            int color
    ) {
        // North (-Z)
        vc.vertex(mat, minX, minY, minZ).color(color);
        vc.vertex(mat, minX, maxY, minZ).color(color);
        vc.vertex(mat, maxX, maxY, minZ).color(color);
        vc.vertex(mat, maxX, minY, minZ).color(color);
        // South (+Z)
        vc.vertex(mat, minX, minY, maxZ).color(color);
        vc.vertex(mat, maxX, minY, maxZ).color(color);
        vc.vertex(mat, maxX, maxY, maxZ).color(color);
        vc.vertex(mat, minX, maxY, maxZ).color(color);
        // West (-X)
        vc.vertex(mat, minX, minY, minZ).color(color);
        vc.vertex(mat, minX, minY, maxZ).color(color);
        vc.vertex(mat, minX, maxY, maxZ).color(color);
        vc.vertex(mat, minX, maxY, minZ).color(color);
        // East (+X)
        vc.vertex(mat, maxX, minY, maxZ).color(color);
        vc.vertex(mat, maxX, minY, minZ).color(color);
        vc.vertex(mat, maxX, maxY, minZ).color(color);
        vc.vertex(mat, maxX, maxY, maxZ).color(color);
        // Top (+Y)
        vc.vertex(mat, minX, maxY, maxZ).color(color);
        vc.vertex(mat, maxX, maxY, maxZ).color(color);
        vc.vertex(mat, maxX, maxY, minZ).color(color);
        vc.vertex(mat, minX, maxY, minZ).color(color);
        // Bottom (-Y)
        vc.vertex(mat, minX, minY, minZ).color(color);
        vc.vertex(mat, maxX, minY, minZ).color(color);
        vc.vertex(mat, maxX, minY, maxZ).color(color);
        vc.vertex(mat, minX, minY, maxZ).color(color);
    }
}
