/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.items.game.CraftedConsumableItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class CraftedConsumableAnnotator implements ItemAnnotator {
    private static final Pattern CRAFTED_CONSUMABLE_PATTERN = Pattern.compile("^§3(.*)§b \\[(\\d+)/(\\d+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher matcher = CRAFTED_CONSUMABLE_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        String craftedName = matcher.group(1);
        int uses = Integer.parseInt(matcher.group(2));
        int maxUses = Integer.parseInt(matcher.group(3));

        return new CraftedConsumableItem(craftedName, new CappedValue(uses, maxUses));
    }
}
