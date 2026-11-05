package com.jigneshgaming.fluxclient.mixin;

import com.jigneshgaming.fluxclient.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {

    @Inject(method = "tick", at = @At("TAIL"))
    private void flux$applySprintAndToggleSneak(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || this.playerInput == null) {
            return;
        }
        ModuleManager mm = ModuleManager.getInstance();
        boolean forceSneak = mm.toggleSneak.shouldApplySneak(client);
        boolean effectiveSneak = this.playerInput.sneak() || forceSneak;
        boolean forceSprint = !effectiveSneak && mm.autoSprint.shouldForceSprint(client);
        boolean effectiveSprint = this.playerInput.sprint() || forceSprint;

        if (effectiveSneak != this.playerInput.sneak() || effectiveSprint != this.playerInput.sprint()) {
            this.playerInput = new PlayerInput(
                    this.playerInput.forward(),
                    this.playerInput.backward(),
                    this.playerInput.left(),
                    this.playerInput.right(),
                    this.playerInput.jump(),
                    effectiveSneak,
                    effectiveSprint
            );
        }
    }
}
