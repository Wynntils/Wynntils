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
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;

public final class CraftedConsumableAnnotator implements GameItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(WynnItemParser.CRAFTED_ITEM_NAME_PATTERN);
        if (!matcher.matches()) return null;

        WynnItemParseResult parseResult = WynnItemParser.parseItemStack(itemStack, null);
        CraftedItemParseResults craftedParseResults = WynnItemParser.parseCraftedItem(itemStack);

        if (parseResult == null || craftedParseResults == null) return null;

        // Consumable items must have a uses value, otherwise it's an other type of crafted item
        if (craftedParseResults.uses() == null) return null;

        return new CraftedConsumableItem(
                craftedParseResults.name(),
                ConsumableType.fromString(parseResult.itemType()),
                parseResult.level(),
                parseResult.identifications(),
                parseResult.namedEffects(),
                parseResult.effects(),
                craftedParseResults.uses());
    }
}
