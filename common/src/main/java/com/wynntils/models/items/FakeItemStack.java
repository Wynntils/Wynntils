/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items;

import com.wynntils.mc.extension.ItemStackExtension;
import com.wynntils.models.gearinfo.tooltip.GearTooltipBuilder;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.wynn.WynnItemUtils;
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
        ((ItemStackExtension) this).setAnnotation(gearItem);

        this.gearItem = gearItem;
        this.source = source;
    }

    public FakeItemStack(GearItem gearItem, String source) {
        this(gearItem, gearItem.getGearInfo().metaInfo().material().asItemStack(), source);
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        GearTooltipBuilder tooltipBuilder = GearTooltipBuilder.fromGearItem(gearItem);
        List<Component> tooltip = tooltipBuilder.getTooltipLines(WynnItemUtils.getCurrentIdentificationStyle());
        // Add a line describing the source of this fake stack
        tooltip.add(
                1, Component.literal(source).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        return tooltip;
    }
}
