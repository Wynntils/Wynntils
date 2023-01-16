/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ArmSwingEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @WrapWithCondition(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/player/Player;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private boolean onSwingInInventoryScreen(Player player, InteractionHand hand) {
        return EventFactory.onArmSwing(ArmSwingEvent.ArmSwingContext.DROP_ITEM_FROM_INVENTORY_SCREEN, hand)
                .isCanceled();
    }
}
