package com.jigneshgaming.fluxclient.mixin;

import com.jigneshgaming.fluxclient.optimization.LowEndOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderManager.class)
public abstract class BlockEntityRenderManagerMixin {
    @Shadow
    private Vec3d cameraPos;

    @Inject(method = "getRenderState", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity, S extends BlockEntityRenderState> void flux$cullDistantBlockEntities(
            E blockEntity,
            float tickProgress,
            ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay,
            CallbackInfoReturnable<S> cir
    ) {
        if (!LowEndOptimizer.getInstance().shouldRenderBlockEntity(blockEntity, this.cameraPos)) {
            cir.setReturnValue(null);
        }
    }
}
