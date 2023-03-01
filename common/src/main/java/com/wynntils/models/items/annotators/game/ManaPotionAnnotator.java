/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.ManaPotionItem;
import com.wynntils.models.wynnitem.parsing.WynnItemParseResult;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class ManaPotionAnnotator implements ItemAnnotator {
    private static final Pattern MANA_POTION_PATTERN = Pattern.compile("^§bPotion of Mana§3 \\[(\\d+)/(\\d+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher matcher = MANA_POTION_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        int uses = Integer.parseInt(matcher.group(1));
        int maxUses = Integer.parseInt(matcher.group(2));

        WynnItemParseResult parseResult = WynnItemParser.parseItemStack(itemStack, null);

        return new ManaPotionItem(parseResult.effects(), new CappedValue(uses, maxUses));
    }
}
