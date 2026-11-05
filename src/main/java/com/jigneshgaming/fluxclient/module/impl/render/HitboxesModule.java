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
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

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
        if (!isEnabled()) {
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
        MatrixStack matrices = context.matrices();
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

            Box rawBox = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
            int color = resolveEntityColor(entity);

            VertexRendering.drawOutline(
                    matrices,
                    lineBuffer,
                    VoxelShapes.cuboid(rawBox),
                    ix,
                    iy,
                    iz,
                    color,
                    width
            );

            if (eyeHeightSetting.get() && entity instanceof LivingEntity) {
                double eyeH = entity.getStandingEyeHeight();
                Box eyeBox = new Box(rawBox.minX, eyeH - 0.01, rawBox.minZ, rawBox.maxX, eyeH + 0.01, rawBox.maxZ);
                VertexRendering.drawOutline(
                        matrices,
                        lineBuffer,
                        VoxelShapes.cuboid(eyeBox),
                        ix,
                        iy,
                        iz,
                        0xFFFF3B30,
                        width
                );
            }

            if (lookVectorSetting.get()) {
                Vec3d rot = entity.getRotationVec(tickDelta);
                float sx = (float) ix;
                float sy = (float) (iy + entity.getStandingEyeHeight());
                float sz = (float) iz;
                float ex = (float) (sx + rot.x * 2.0);
                float ey = (float) (sy + rot.y * 2.0);
                float ez = (float) (sz + rot.z * 2.0);
                MatrixStack.Entry entry = matrices.peek();
                lineBuffer.vertex(entry, sx, sy, sz)
                        .color(0xFF3399FF)
                        .normal(entry, (float) rot.x, (float) rot.y, (float) rot.z)
                        .lineWidth(width);
                lineBuffer.vertex(entry, ex, ey, ez)
                        .color(0xFF3399FF)
                        .normal(entry, (float) rot.x, (float) rot.y, (float) rot.z)
                        .lineWidth(width);
            }
        }
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
