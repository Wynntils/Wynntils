/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ItemToolTipHoveredNameEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Redirect(
            method =
                    "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;"))
    public Component redirectGetHoveredName(ItemStack instance) {
        ItemToolTipHoveredNameEvent event =
                new ItemToolTipHoveredNameEvent(instance.getHoverName(), instance);
        WynntilsMod.getEventBus().post(event);
        return event.getHoveredName();
    }
}
