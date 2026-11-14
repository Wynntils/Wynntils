/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.augment;

import com.wynntils.core.components.Services;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.builders.InsulatorTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.InsulatorItem;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

public class GuideInsulatorItemStack extends GuideAugmentItemStack {
    public GuideInsulatorItemStack() {
        super(getItemStack(), new InsulatorItem(GearTier.MYTHIC), "Corkian Insulator", GearTier.MYTHIC);
    }

    @Override
    public Component getHoverName() {
        return Component.literal("Corkian Insulator").withStyle(getGearTier().getChatFormatting());
    }

    @Override
    protected TooltipBuilder createTooltipBuilder() {
        return InsulatorTooltipBuilder.buildNewItem(getGearTier(), ItemSourceType.GUIDE, "");
    }

    private static ItemStack getItemStack() {
        ItemStack itemStack = new ItemStack(Items.POTION);

        CustomModelData customModelData = new CustomModelData(
                List.of(Services.CustomModel.getFloat("corkian_insulator").orElse(-1f)),
                List.of(),
                List.of(),
                List.of());
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, customModelData);

        return itemStack;
    }

    @Override
    public int getTier() {
        return -1;
    }
}
