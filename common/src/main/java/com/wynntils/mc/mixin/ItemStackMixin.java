/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.mc.event.ItemTooltipFlagsEvent;
import com.wynntils.mc.extension.ItemStackExtension;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.codecs.CustomDataComponents;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.utils.mc.TooltipUtils;
import java.util.List;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
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

    @Inject(
            method =
                    "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true)
    public void getTooltipLines(
            Item.TooltipContext context,
            Player player,
            TooltipFlag isAdvanced,
            CallbackInfoReturnable<List<Component>> cir) {
        WynnItem wynnItem = ((DataComponentHolder) this).get(CustomDataComponents.WYNN_ITEM);
        if (wynnItem == null) return;

        // FIXME: Source should be dynamic
        final String source = "Fake Item";

        // 1. Firstly, cache a tooltip builder with the item's data
        //    This will be used by TooltipUtils to generate the tooltip
        TooltipBuilder tooltipBuilder = null;

        if (wynnItem instanceof IdentifiableItemProperty<?, ?> identifiableItem) {
            tooltipBuilder = wynnItem.getData()
                    .getOrCalculate(
                            WynnItemData.TOOLTIP_KEY,
                            () -> Handlers.Tooltip.buildNew(identifiableItem, false, true, source));

        } else if (wynnItem instanceof CraftedItemProperty craftedItemProperty) {
            tooltipBuilder = wynnItem.getData()
                    .getOrCalculate(
                            WynnItemData.TOOLTIP_KEY, () -> Handlers.Tooltip.buildNew(craftedItemProperty, source));
        }

        if (tooltipBuilder == null) {
            cir.setReturnValue(List.of());
            return;
        }

        // 2. Now that the tooltip builder is cached, generate the tooltip
        cir.setReturnValue(TooltipUtils.getWynnItemTooltip((ItemStack) (Object) this, wynnItem));
    }
}
