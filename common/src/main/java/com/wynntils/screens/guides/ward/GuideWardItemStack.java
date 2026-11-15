/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ward;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.handlers.tooltip.builders.WardTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.items.items.game.WardItem;
import com.wynntils.models.rewards.type.WardType;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.EnumUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;

public class GuideWardItemStack extends GuideItemStack {
    private List<Component> generatedTooltip;
    private final WardType wardType;

    public GuideWardItemStack(WardType wardType) {
        super(getItemStack(wardType), new WardItem(wardType), EnumUtils.toNiceString(wardType) + " Ward");

        this.wardType = wardType;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        if (generatedTooltip == null) {
            generatedTooltip = new ArrayList<>(WardTooltipBuilder.buildNewItem(wardType, ItemSourceType.GUIDE, "")
                    .getTooltipLines(Models.Character.getClassType()));
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

    public WardType getWardType() {
        return wardType;
    }
}
