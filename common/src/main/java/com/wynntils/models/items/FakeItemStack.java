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
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class FakeItemStack extends ItemStack {
    private final WynnItem wynnItem;
    private final String source;

    private FakeItemStack(WynnItem wynnItem, ItemStack itemStack, String source) {
        super(itemStack.getItem(), 1);
        this.setTag(itemStack.getTag());

        if (wynnItem instanceof IdentifiableItemProperty identifiableItemProperty) {
            Handlers.Item.updateItem(this, wynnItem, StyledText.fromString(identifiableItemProperty.getName()));
        }

        this.wynnItem = wynnItem;
        this.source = source;
    }

    public FakeItemStack(GearItem gearItem, String source) {
        this(gearItem, gearItem.getItemInfo().metaInfo().material().itemStack(), source);
    }

    // This should be only used by chat items, so the item does not matter
    public FakeItemStack(WynnItem wynnItem, String source) {
        this(wynnItem, new ItemStack(Items.STONE), source);
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        if (wynnItem instanceof IdentifiableItemProperty<?, ?> identifiableItem) {
            TooltipBuilder tooltipBuilder = Handlers.Tooltip.buildNew(identifiableItem, false);
            List<Component> tooltip = tooltipBuilder.getTooltipLines(Models.Character.getClassType());
            // Add a line describing the source of this fake stack
            tooltip.add(
                    1,
                    Component.literal(source)
                            .withStyle(ChatFormatting.DARK_GRAY)
                            .withStyle(ChatFormatting.ITALIC));
            return tooltip;
        }

        return List.of();
    }
}
