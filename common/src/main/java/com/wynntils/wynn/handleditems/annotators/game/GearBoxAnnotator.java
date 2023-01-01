/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.handleditems.items.game.GearBoxItem;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
import com.wynntils.wynn.objects.profiles.item.ItemType;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class GearBoxAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        if (!WynnItemMatchers.isUnidentified(itemStack)) return null;

        String name = itemStack.getHoverName().getString();

        ItemType itemType = getItemType(name);
        ItemTier itemTier = ItemTier.fromString(name);
        String levelRange = getLevelRange(itemStack);

        if (itemType == null || itemTier == null || levelRange == null) return null;

        return new GearBoxItem(itemType, itemTier, levelRange);
    }

    private static ItemType getItemType(String name) {
        String itemName = WynnUtils.normalizeBadString(ComponentUtils.stripFormatting(name));

        // FIXME: This is dangerous and can crash! Should use regex instead.
        String itemTypeStr = itemName.split(" ", 2)[1];
        if (itemTypeStr == null) return null;

        Optional<ItemType> itemType = ItemType.fromString(itemTypeStr);
        return itemType.orElse(null);
    }

    private static String getLevelRange(ItemStack itemStack) {
        for (Component tooltipLine : itemStack.getTooltipLines(null, TooltipFlag.NORMAL)) {
            String line = WynnUtils.normalizeBadString(tooltipLine.getString());
            if (line.contains("Lv. Range")) {
                return line.replace("- Lv. Range: ", "");
            }
        }
        return null;
    }
}
