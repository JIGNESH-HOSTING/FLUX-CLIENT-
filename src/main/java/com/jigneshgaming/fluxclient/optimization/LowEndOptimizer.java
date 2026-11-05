package com.jigneshgaming.fluxclient.optimization;

import com.jigneshgaming.fluxclient.config.FluxConfigManager;
import com.jigneshgaming.fluxclient.ui.FluxNotificationOverlay;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Genuine low-end Android (3 GB RAM Vivo 1916 / PojavLauncher) & desktop optimization engine.
 * Reduces entity/block-entity render state allocations, GPU draw calls, Gaussian menu blur FBO passes,
 * particle burst spikes, and unfocused CPU/GPU load without any fake FPS numbers or multiplayer breakage.
 */
public final class LowEndOptimizer {
    private static final LowEndOptimizer INSTANCE = new LowEndOptimizer();

    private boolean entityCullingEnabled = true;
    private int passiveEntityMaxDistance = 40;
    private int itemEntityMaxDistance = 24;

    private boolean blockEntityCullingEnabled = true;
    private int blockEntityMaxDistance = 36;

    private boolean particleLimiterEnabled = true;
    private int maxParticlesPerTick = 48;
    private int particleMaxDistance = 28;

    private boolean menuBlurBypass = true;
    private boolean unfocusedFpsLimiter = true;
    private int unfocusedMaxFps = 30;

    private int particlesSpawnedThisTick = 0;
    private long lastHeapCheckMs = 0L;

    private LowEndOptimizer() {
    }

    public static LowEndOptimizer getInstance() {
        return INSTANCE;
    }

    public void onClientTick(MinecraftClient client) {
        particlesSpawnedThisTick = 0;

        long now = System.currentTimeMillis();
        if (now - lastHeapCheckMs >= 10_000L) {
            lastHeapCheckMs = now;
            Runtime rt = Runtime.getRuntime();
            long max = Math.max(1L, rt.maxMemory());
            long used = rt.totalMemory() - rt.freeMemory();
            if ((double) used / (double) max > 0.88) {
                // Trim transient client UI buffers safely without triggering stop-the-world System.gc()
                if (client != null && client.world == null) {
                    FluxNotificationOverlay.clear();
                }
            }
        }
    }

    /**
     * Skips rendering non-critical distant entities on this frame to cut state allocations & GPU draw calls.
     * Never culls players, bosses, glowing entities, ridden vehicles, or custom-named entities.
     */
    public boolean shouldRenderEntity(Entity entity, double camX, double camY, double camZ) {
        if (!entityCullingEnabled || entity == null) {
            return true;
        }
        if (entity instanceof PlayerEntity
                || entity instanceof EnderDragonEntity
                || entity instanceof WitherEntity
                || entity.hasPassengers()
                || entity.hasVehicle()
                || entity.isGlowing()
                || entity.hasCustomName()) {
            return true;
        }

        double dx = entity.getX() - camX;
        double dy = entity.getY() - camY;
        double dz = entity.getZ() - camZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity) {
            double max = itemEntityMaxDistance;
            return distSq <= max * max;
        }

        if (entity instanceof AmbientEntity
                || entity instanceof PassiveEntity
                || entity instanceof ArmorStandEntity
                || entity instanceof ItemFrameEntity) {
            double max = passiveEntityMaxDistance;
            return distSq <= max * max;
        }

        double generalMax = Math.max(48.0, passiveEntityMaxDistance + 16.0);
        return distSq <= generalMax * generalMax;
    }

    /**
     * Skips distant chests, signs, skulls, banners, and pots beyond blockEntityMaxDistance
     * while preserving beacons and portals. Saves per-frame BlockEntityRenderState allocations on 3GB RAM devices.
     */
    public boolean shouldRenderBlockEntity(BlockEntity blockEntity, Vec3d camPos) {
        if (!blockEntityCullingEnabled || blockEntity == null || camPos == null) {
            return true;
        }
        if (blockEntity instanceof BeaconBlockEntity
                || blockEntity instanceof EndPortalBlockEntity
                || blockEntity instanceof EndGatewayBlockEntity) {
            return true;
        }
        BlockPos pos = blockEntity.getPos();
        if (pos == null) {
            return true;
        }
        double dx = (pos.getX() + 0.5) - camPos.x;
        double dy = (pos.getY() + 0.5) - camPos.y;
        double dz = (pos.getZ() + 0.5) - camPos.z;
        double max = blockEntityMaxDistance;
        return (dx * dx + dy * dy + dz * dz) <= (max * max);
    }

    /**
     * Caps per-tick particle bursts (e.g. explosions/weather) and distant particles on mobile GPUs.
     */
    public boolean shouldAllowParticle(double x, double y, double z) {
        if (!particleLimiterEnabled) {
            return true;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            double distSq = client.player.squaredDistanceTo(x, y, z);
            double maxD = particleMaxDistance;
            if (distSq > maxD * maxD) {
                return false;
            }
        }
        if (particlesSpawnedThisTick >= maxParticlesPerTick) {
            return false;
        }
        particlesSpawnedThisTick++;
        return true;
    }

    /**
     * Applies non-destructive vanilla video settings tuned for 3 GB RAM Android 9 (PojavLauncher) & low-end PCs.
     */
    public void applyLowEndVideoProfile(MinecraftClient client) {
        if (client == null || client.options == null) {
            return;
        }
        try {
            long maxHeapMb = Runtime.getRuntime().maxMemory() / (1024L * 1024L);
            int targetViewChunks = maxHeapMb <= 1450L ? 5 : 6;

            client.options.applyGraphicsMode(GraphicsMode.FAST);
            client.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
            client.options.getParticles().setValue(ParticlesMode.DECREASED);
            client.options.getEntityShadows().setValue(false);
            client.options.getBiomeBlendRadius().setValue(0);
            client.options.getMenuBackgroundBlurriness().setValue(0);
            if (client.options.getViewDistance().getValue() > targetViewChunks) {
                client.options.getViewDistance().setValue(targetViewChunks);
            }
            if (client.options.getSimulationDistance().getValue() > 5) {
                client.options.getSimulationDistance().setValue(5);
            }
            client.options.getEntityDistanceScaling().setValue(0.70);
            client.options.write();

            this.entityCullingEnabled = true;
            this.passiveEntityMaxDistance = 36;
            this.itemEntityMaxDistance = 22;
            this.blockEntityCullingEnabled = true;
            this.blockEntityMaxDistance = 32;
            this.particleLimiterEnabled = true;
            this.maxParticlesPerTick = 36;
            this.particleMaxDistance = 24;
            this.menuBlurBypass = true;
            FluxConfigManager.saveAsync();

            FluxNotificationOverlay.push(
                    "Low-End Profile Applied",
                    "Tuned for " + targetViewChunks + "-chunk view, entity/tile/particle culling & 0 menu blur",
                    FluxNotificationOverlay.Type.SUCCESS
            );
        } catch (Throwable ignored) {
        }
    }

    public boolean isEntityCullingEnabled() {
        return entityCullingEnabled;
    }

    public void setEntityCullingEnabled(boolean entityCullingEnabled) {
        this.entityCullingEnabled = entityCullingEnabled;
    }

    public int getPassiveEntityMaxDistance() {
        return passiveEntityMaxDistance;
    }

    public void setPassiveEntityMaxDistance(int passiveEntityMaxDistance) {
        this.passiveEntityMaxDistance = Math.max(16, Math.min(96, passiveEntityMaxDistance));
    }

    public int getItemEntityMaxDistance() {
        return itemEntityMaxDistance;
    }

    public void setItemEntityMaxDistance(int itemEntityMaxDistance) {
        this.itemEntityMaxDistance = Math.max(12, Math.min(64, itemEntityMaxDistance));
    }

    public boolean isBlockEntityCullingEnabled() {
        return blockEntityCullingEnabled;
    }

    public void setBlockEntityCullingEnabled(boolean blockEntityCullingEnabled) {
        this.blockEntityCullingEnabled = blockEntityCullingEnabled;
    }

    public int getBlockEntityMaxDistance() {
        return blockEntityMaxDistance;
    }

    public void setBlockEntityMaxDistance(int blockEntityMaxDistance) {
        this.blockEntityMaxDistance = Math.max(16, Math.min(64, blockEntityMaxDistance));
    }

    public boolean isParticleLimiterEnabled() {
        return particleLimiterEnabled;
    }

    public void setParticleLimiterEnabled(boolean particleLimiterEnabled) {
        this.particleLimiterEnabled = particleLimiterEnabled;
    }

    public int getMaxParticlesPerTick() {
        return maxParticlesPerTick;
    }

    public void setMaxParticlesPerTick(int maxParticlesPerTick) {
        this.maxParticlesPerTick = Math.max(10, Math.min(200, maxParticlesPerTick));
    }

    public int getParticleMaxDistance() {
        return particleMaxDistance;
    }

    public void setParticleMaxDistance(int particleMaxDistance) {
        this.particleMaxDistance = Math.max(12, Math.min(64, particleMaxDistance));
    }

    public boolean isMenuBlurBypass() {
        return menuBlurBypass;
    }

    public void setMenuBlurBypass(boolean menuBlurBypass) {
        this.menuBlurBypass = menuBlurBypass;
    }

    public boolean isUnfocusedFpsLimiter() {
        return unfocusedFpsLimiter;
    }

    public void setUnfocusedFpsLimiter(boolean unfocusedFpsLimiter) {
        this.unfocusedFpsLimiter = unfocusedFpsLimiter;
    }

    public int getUnfocusedMaxFps() {
        return unfocusedMaxFps;
    }

    public void setUnfocusedMaxFps(int unfocusedMaxFps) {
        this.unfocusedMaxFps = Math.max(15, Math.min(60, unfocusedMaxFps));
    }
}
