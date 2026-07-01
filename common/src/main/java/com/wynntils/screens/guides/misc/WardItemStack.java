/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.misc;

import com.wynntils.core.components.Services;
import com.wynntils.models.items.items.game.WardItem;
import com.wynntils.models.rewards.type.ItemObtainTypes;
import com.wynntils.models.rewards.type.WardType;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.EnumUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;

public class WardItemStack extends GuideItemStack {
    private List<Component> generatedTooltip;
    private final WardType wardType;

    public WardItemStack(WardType wardType) {
        super(getItemStack(wardType), new WardItem(wardType), EnumUtils.toNiceString(wardType) + " Ward");

        this.wardType = wardType;
    }

    @Override
    public Component getHoverName() {
        return Component.literal(EnumUtils.toNiceString(wardType) + " Ward")
                .withColor(getWardType().getColor().asInt());
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(getHoverName());
            tooltip.add(Component.empty());
            tooltip.addAll(generateLore());

            appendFavoriteInfo(tooltip);

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    private static ItemStack getItemStack(WardType wardType) {
        ItemStack itemStack = new ItemStack(Items.POTION);

        CustomModelData customModelData = new CustomModelData(
                List.of(Services.CustomModel.getFloat("ward_" + wardType.name().toLowerCase(Locale.ROOT))
                        .orElse(-1f)),
                List.of(),
                List.of(),
                List.of());
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, customModelData);

        return itemStack;
    }

    private List<Component> generateLore() {
        MutableComponent usageComponent = Component.translatable("screens.wynntils.wynntilsGuides.misc.wards.usage1")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.wards.usage2")
                        .withStyle(ChatFormatting.WHITE));
        MutableComponent obtainComponent = ItemObtainTypes.component(wardType.getItemObtainTypes());

        return List.of(usageComponent, Component.empty(), obtainComponent);
    }

    public WardType getWardType() {
        return wardType;
    }
}
