package com.jigneshgaming.fluxclient.module.impl.render;

import com.jigneshgaming.fluxclient.module.Module;
import com.jigneshgaming.fluxclient.module.ModuleCategory;
import com.jigneshgaming.fluxclient.module.ModuleIcon;
import com.jigneshgaming.fluxclient.module.setting.BooleanSetting;
import com.jigneshgaming.fluxclient.module.setting.ColorSetting;
import com.jigneshgaming.fluxclient.module.setting.NumberSetting;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class HitboxesModule extends Module {
    private final BooleanSetting playersOnlySetting;
    private final BooleanSetting eyeHeightSetting;
    private final BooleanSetting lookVectorSetting;
    private final BooleanSetting entityTypeColorSetting;
    private final ColorSetting hitboxColorSetting;
    private final NumberSetting lineWidthSetting;
    private final NumberSetting maxDistanceSetting;

    public HitboxesModule() {
        super(
                "hitboxes",
                "Hitboxes",
                "Renders crisp 3D entity bounding boxes, eye-height rings, and look vectors.",
                ModuleCategory.RENDER,
                ModuleIcon.HITBOXES,
                false
        );
        this.playersOnlySetting = addSetting(new BooleanSetting("playersOnly", "Players Only", "Only draw hitboxes for player entities", false));
        this.eyeHeightSetting = addSetting(new BooleanSetting("eyeHeight", "Eye Height Ring", "Draw red eye-level bounding ring", true));
        this.lookVectorSetting = addSetting(new BooleanSetting("lookVector", "Look Vector", "Draw blue entity look direction ray", true));
        this.entityTypeColorSetting = addSetting(new BooleanSetting("typeColors", "Color By Type", "Color players, hostiles, and passives distinctly", false));
        this.hitboxColorSetting = addSetting(new ColorSetting("color", "Hitbox Color", "Primary hitbox outline color", "White"));
        this.lineWidthSetting = addSetting(new NumberSetting("lineWidth", "Line Width", "Hitbox line thickness", 1.8, 1.0, 4.0, 0.2, "px"));
        this.maxDistanceSetting = addSetting(new NumberSetting("maxDistance", "Max Distance", "Maximum hitbox render distance", 48, 16, 96, 4, "m"));
    }

    public void renderHitboxes(WorldRenderContext context) {
        if (!isEnabled() || context.consumers() == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return;
        }

        Vec3d cam = context.worldState().cameraRenderState.pos;
        float tickDelta = client.getRenderTickCounter().getTickProgress(false);
        double maxDistSq = maxDistanceSetting.get() * maxDistanceSetting.get();
        float width = lineWidthSetting.getFloat();
        MatrixStack.Entry entry = context.matrices().peek();
        VertexConsumer lineBuffer = context.consumers().getBuffer(RenderLayers.lines());

        for (Entity entity : client.world.getEntities()) {
            if (entity == null || entity.isSpectator() || !entity.isAlive()) {
                continue;
            }
            if (entity == client.player && !client.gameRenderer.getCamera().isThirdPerson()) {
                continue;
            }
            if (playersOnlySetting.get() && !(entity instanceof PlayerEntity)) {
                continue;
            }
            if (entity.squaredDistanceTo(cam) > maxDistSq) {
                continue;
            }

            double ix = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()) - cam.x;
            double iy = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()) - cam.y;
            double iz = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()) - cam.z;

            Box bb = entity.getBoundingBox();
            double halfX = (bb.maxX - bb.minX) * 0.5;
            double heightY = bb.maxY - bb.minY;
            double halfZ = (bb.maxZ - bb.minZ) * 0.5;

            float minX = (float) (ix - halfX);
            float minY = (float) iy;
            float minZ = (float) (iz - halfZ);
            float maxX = (float) (ix + halfX);
            float maxY = (float) (iy + heightY);
            float maxZ = (float) (iz + halfZ);

            int color = resolveEntityColor(entity);
            drawBoxEdges(entry, lineBuffer, minX, minY, minZ, maxX, maxY, maxZ, color, width);

            if (eyeHeightSetting.get() && entity instanceof LivingEntity) {
                float eyeY = (float) (iy + entity.getStandingEyeHeight());
                drawBoxEdges(entry, lineBuffer, minX, eyeY - 0.01f, minZ, maxX, eyeY + 0.01f, maxZ, 0xFFFF3B30, width);
            }

            if (lookVectorSetting.get()) {
                Vec3d rot = entity.getRotationVec(tickDelta);
                float sx = (float) ix;
                float sy = (float) (iy + entity.getStandingEyeHeight());
                float sz = (float) iz;
                float ex = (float) (sx + rot.x * 2.0);
                float ey = (float) (sy + rot.y * 2.0);
                float ez = (float) (sz + rot.z * 2.0);
                drawLine(entry, lineBuffer, sx, sy, sz, ex, ey, ez, (float) rot.x, (float) rot.y, (float) rot.z, 0xFF3399FF, width);
            }
        }
    }

    /**
     * Zero-allocation 12-edge cuboid wireframe writer (saves VoxelShapes.cuboid heap allocations on 3GB RAM devices).
     */
    private static void drawBoxEdges(
            MatrixStack.Entry entry,
            VertexConsumer vc,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            int color,
            float width
    ) {
        // Bottom rectangle
        drawLine(entry, vc, x1, y1, z1, x2, y1, z1, 1f, 0f, 0f, color, width);
        drawLine(entry, vc, x2, y1, z1, x2, y1, z2, 0f, 0f, 1f, color, width);
        drawLine(entry, vc, x2, y1, z2, x1, y1, z2, -1f, 0f, 0f, color, width);
        drawLine(entry, vc, x1, y1, z2, x1, y1, z1, 0f, 0f, -1f, color, width);
        // Top rectangle
        drawLine(entry, vc, x1, y2, z1, x2, y2, z1, 1f, 0f, 0f, color, width);
        drawLine(entry, vc, x2, y2, z1, x2, y2, z2, 0f, 0f, 1f, color, width);
        drawLine(entry, vc, x2, y2, z2, x1, y2, z2, -1f, 0f, 0f, color, width);
        drawLine(entry, vc, x1, y2, z2, x1, y2, z1, 0f, 0f, -1f, color, width);
        // Vertical pillars
        drawLine(entry, vc, x1, y1, z1, x1, y2, z1, 0f, 1f, 0f, color, width);
        drawLine(entry, vc, x2, y1, z1, x2, y2, z1, 0f, 1f, 0f, color, width);
        drawLine(entry, vc, x2, y1, z2, x2, y2, z2, 0f, 1f, 0f, color, width);
        drawLine(entry, vc, x1, y1, z2, x1, y2, z2, 0f, 1f, 0f, color, width);
    }

    private static void drawLine(
            MatrixStack.Entry entry,
            VertexConsumer vc,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float nx, float ny, float nz,
            int color,
            float width
    ) {
        vc.vertex(entry, x1, y1, z1).color(color).normal(entry, nx, ny, nz).lineWidth(width);
        vc.vertex(entry, x2, y2, z2).color(color).normal(entry, nx, ny, nz).lineWidth(width);
    }

    private int resolveEntityColor(Entity entity) {
        if (entityTypeColorSetting.get()) {
            if (entity instanceof PlayerEntity) {
                return 0xFF2EA8FF;
            } else if (entity instanceof Monster) {
                return 0xFFE74C3C;
            }
            return 0xFF2ECC71;
        }
        return hitboxColorSetting.resolveArgb(240);
    }
}
