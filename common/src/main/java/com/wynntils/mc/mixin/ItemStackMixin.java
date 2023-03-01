/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ItemTooltipFlagsEvent;
import com.wynntils.mc.event.ItemTooltipHoveredNameEvent;
import com.wynntils.mc.extension.ItemStackExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtension {
    @Unique
    private ItemAnnotation wynntilsAnnotation;

    @Unique
    private String wynntilsOriginalName;

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

    @Redirect(
            method =
                    "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getHideFlags()I"))
    private int redirectGetHideFlags(ItemStack instance) {
        ItemStack itemStack = (ItemStack) (Object) this;
        ItemTooltipFlagsEvent.Mask event = EventFactory.onTooltipFlagsMask(itemStack, instance.getHideFlags());

        return event.getMask();
    }

    @ModifyVariable(
            method =
                    "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private TooltipFlag onGetTooltipLines(TooltipFlag flags) {
        ItemStack itemStack = (ItemStack) (Object) this;
        ItemTooltipFlagsEvent.Advanced event = EventFactory.onTooltipFlagsAdvanced(itemStack, flags);
        return event.getFlags();
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

    @Override
    @Unique
    public String getOriginalName() {
        return this.wynntilsOriginalName;
    }

    @Override
    @Unique
    public void setOriginalName(String name) {
        this.wynntilsOriginalName = name;
    }
}
