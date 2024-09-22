/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.ConsumableType;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.wynnitem.parsing.CraftedItemParseResults;
import com.wynntils.models.wynnitem.parsing.WynnItemParseResult;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class CraftedConsumableAnnotator implements GameItemAnnotator {
    private static final Pattern CRAFTED_CONSUMABLE_PATTERN = Pattern.compile("^§3(.*)§b \\[(\\d+)/(\\d+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(CRAFTED_CONSUMABLE_PATTERN);
        if (!matcher.matches()) return null;

        String craftedName = matcher.group(1);
        int uses = Integer.parseInt(matcher.group(2));
        int maxUses = Integer.parseInt(matcher.group(3));

        WynnItemParseResult parseResult = WynnItemParser.parseItemStack(itemStack, null);
        CraftedItemParseResults craftedParseResults = WynnItemParser.parseCraftedItem(itemStack);

        return new CraftedConsumableItem(
                craftedName,
                ConsumableType.fromString(parseResult.itemType()),
                parseResult.level(),
                parseResult.identifications(),
                parseResult.namedEffects(),
                parseResult.effects(),
                new CappedValue(uses, maxUses));
    }
}
