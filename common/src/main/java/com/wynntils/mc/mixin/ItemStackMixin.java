/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.mc.event.ItemTooltipFlagsEvent;
import com.wynntils.mc.event.ItemTooltipLinesEvent;
import com.wynntils.mc.extension.ItemStackExtension;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtension {
    @Unique
    private ItemAnnotation wynntilsAnnotation;

    @Unique
    private StyledText wynntilsOriginalName;

    @ModifyVariable(
            method =
                    "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private TooltipFlag onGetTooltipLines(TooltipFlag flags) {
        ItemStack itemStack = (ItemStack) (Object) this;
        ItemTooltipFlagsEvent event = new ItemTooltipFlagsEvent(itemStack, flags);
        MixinHelper.post(event);

        return event.getFlags();
    }

    @Inject(
            method =
                    "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetTooltipLines(
            Item.TooltipContext context,
            net.minecraft.world.entity.player.Player player,
            TooltipFlag flags,
            CallbackInfoReturnable<List<Component>> callbackInfo) {
        ItemStack itemStack = (ItemStack) (Object) this;
        ItemTooltipLinesEvent event = new ItemTooltipLinesEvent(itemStack, callbackInfo.getReturnValue());
        MixinHelper.post(event);

        callbackInfo.setReturnValue(event.getTooltipLines());
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
    public StyledText getOriginalName() {
        return this.wynntilsOriginalName;
    }

    @Override
    @Unique
    public void setOriginalName(StyledText name) {
        this.wynntilsOriginalName = name;
    }
}
