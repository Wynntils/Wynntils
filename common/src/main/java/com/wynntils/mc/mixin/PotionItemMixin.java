/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.DrawPotionGlintEvent;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {
    @ModifyReturnValue(method = "isFoil(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    private boolean isFoilPre(boolean original) {
        DrawPotionGlintEvent event = MixinHelper.post(new DrawPotionGlintEvent((PotionItem) (Object) this));
        if (event.isCanceled()) {
            return false;
        }

        return original;
    }
}
