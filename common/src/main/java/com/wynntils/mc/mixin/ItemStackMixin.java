/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.handlers.item.AnnotatedItemStack;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ItemTooltipHoveredNameEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements AnnotatedItemStack {
    @Unique
    private ItemAnnotation wynntilsAnnotation;

    @Redirect(
            method =
                    "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;"))
    private Component redirectGetHoveredName(ItemStack instance) {
        ItemTooltipHoveredNameEvent event = EventFactory.onGetHoverName(instance.getHoverName(), instance);
        return event.getHoveredName();
    }

    @Override
    @Unique
    public ItemAnnotation getAnnotation() {
        return wynntilsAnnotation;
    }

    @Override
    @Unique
    public void setAnnotation(ItemAnnotation annotation) {
        this.wynntilsAnnotation = annotation;
    }
}
