/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.GameItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public abstract class GameItemAnnotator implements ItemAnnotator {
    private static final Pattern PRICE_STR = Pattern.compile("§6Price:");

    // Test suite: https://regexr.com/7lh2b
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "§[67] - (?:§f(?<amount>[\\d,]+) §7x )?§(?:(?:(?:c✖|a✔) §f)|f)(?<price>[\\d,]+)§7²(?: .+)?");

    public abstract GameItem getAnnotation(
            ItemStack itemStack, StyledText name, List<StyledText> lore, int emeraldPrice);

    @Override
    public final ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        int emeraldPrice = parsePrice(lore);

        return getAnnotation(itemStack, name, lore, emeraldPrice);
    }

    private int parsePrice(List<StyledText> lore) {
        boolean foundPrice = false;
        for (StyledText loreLine : lore) {
            if (loreLine.matches(PRICE_STR)) {
                foundPrice = true;
                continue;
            }

            if (foundPrice) {
                Matcher matcher = loreLine.getMatcher(PRICE_PATTERN);
                if (matcher.matches()) {
                    return Integer.parseInt(matcher.group("price").replaceAll(",", ""));
                } else {
                    WynntilsMod.warn("Found price line, but could not match price pattern." + loreLine);
                }

                // We are done either way
                break;
            }
        }

        return 0;
    }
}
