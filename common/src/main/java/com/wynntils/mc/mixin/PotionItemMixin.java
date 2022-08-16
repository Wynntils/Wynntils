/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.DrawPotionGlintEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {
    @Inject(method = "isFoil(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void isFoilPre(ItemStack item, CallbackInfoReturnable<Boolean> cir) {
        DrawPotionGlintEvent event = EventFactory.onPotionIsFoil((PotionItem) (Object) this);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
