/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.augment;

import com.wynntils.core.components.Services;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.InsulatorItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

public class InsulatorItemStack extends AugmentItemStack {
    public InsulatorItemStack() {
        super(getItemStack(), new InsulatorItem(GearTier.MYTHIC), "Corkian Insulator", GearTier.MYTHIC);
    }

    @Override
    public Component getHoverName() {
        return Component.literal("Corkian Insulator").withStyle(getGearTier().getChatFormatting());
    }

    @Override
    public List<Component> generateLore() {
        MutableComponent effectComponent = Component.translatable("screens.wynntils.wynntilsGuides.augments.insulator1")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.augments.insulator2")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("screens.wynntils.wynntilsGuides.augments.insulator3")
                        .withStyle(ChatFormatting.GRAY));
        MutableComponent incompatibleComponent = Component.translatable(
                        "screens.wynntils.wynntilsGuides.augments.insulator4")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.augments.insulator5")
                        .withStyle(ChatFormatting.GRAY));
        MutableComponent obtainComponent = Component.translatable("screens.wynntils.wynntilsGuides.augments.obtain")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.augments.obtainRaids")
                        .withStyle(ChatFormatting.AQUA));

        return List.of(effectComponent, Component.empty(), incompatibleComponent, Component.empty(), obtainComponent);
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
