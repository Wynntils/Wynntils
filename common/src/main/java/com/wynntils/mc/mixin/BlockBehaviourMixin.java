/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @Inject(
            method =
                    "getRenderShape(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/RenderShape;",
            at = @At("HEAD"),
            cancellable = true)
    private void hideTripwire(BlockState blockState, CallbackInfoReturnable<RenderShape> cir) {
        if (!MixinHelper.onWynncraft()) return;

        // Set tripwires as invisible as the resource pack makes them invisible but when using Sodium they become black
        // lines
        if (blockState.is(Blocks.TRIPWIRE)) {
            cir.setReturnValue(RenderShape.INVISIBLE);
        }
    }
}
