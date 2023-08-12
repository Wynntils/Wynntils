/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.wynnitem.parsing.WynnItemParseResult;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class CraftedConsumableAnnotator implements ItemAnnotator {
    private static final Pattern CRAFTED_CONSUMABLE_PATTERN = Pattern.compile("^§3(.*)§b \\[(\\d+)/(\\d+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(CRAFTED_CONSUMABLE_PATTERN);
        if (!matcher.matches()) return null;

        String craftedName = matcher.group(1);
        int uses = Integer.parseInt(matcher.group(2));
        int maxUses = Integer.parseInt(matcher.group(3));

        WynnItemParseResult parseResult = WynnItemParser.parseItemStack(itemStack, null);

        return new CraftedConsumableItem(
                craftedName,
                parseResult.level(),
                parseResult.identifications(),
                parseResult.effects(),
                new CappedValue(uses, maxUses));
    }
}
