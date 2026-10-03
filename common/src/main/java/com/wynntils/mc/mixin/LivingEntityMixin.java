/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ArmSwingEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @WrapWithCondition(
            method =
                    "drop(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/util/Prediction;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/LivingEntity;swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z"))
    private boolean onSwingInInventoryScreen(
            LivingEntity entity, InteractionHand hand, SwingAnimation animation, boolean sendToSwingingEntity) {
        ArmSwingEvent event = new ArmSwingEvent(ArmSwingEvent.ArmSwingContext.DROP_ITEM_FROM_INVENTORY_SCREEN, hand);
        MixinHelper.post(event);

        return !event.isCanceled();
    }
}
