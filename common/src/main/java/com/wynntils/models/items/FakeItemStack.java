/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.models.items.items.game.GearItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class FakeItemStack extends ItemStack {
    private final GearItem gearItem;
    private final String source;

    private FakeItemStack(GearItem gearItem, ItemStack itemStack, String source) {
        super(itemStack.getItem(), 1);
        this.setTag(itemStack.getTag());
        Handlers.Item.updateItem(
                this, gearItem, StyledText.fromString(gearItem.getGearInfo().name()));

        this.gearItem = gearItem;
        this.source = source;
    }

    public FakeItemStack(GearItem gearItem, String source) {
        this(gearItem, gearItem.getGearInfo().metaInfo().material().itemStack(), source);
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        TooltipBuilder tooltipBuilder = Handlers.Tooltip.buildNew(gearItem, false);
        List<Component> tooltip = tooltipBuilder.getTooltipLines(Models.Character.getClassType());
        // Add a line describing the source of this fake stack
        tooltip.add(
                1, Component.literal(source).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        return tooltip;
    }
}
