/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.GameItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public abstract class GameItemAnnotator implements ItemAnnotator {
    private static final int TRADE_MARKET_PRICE_LINE = 1;
    private static final Pattern PRICE_STR = Pattern.compile("§6Price:");

    // Test suite: https://regexr.com/7lh2b
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "§[67] - (?:§f(?<amount>[\\d,]+) §7x )?§(?:(?:(?:c✖|a✔) §f)|f§m|f)(?<price>[\\d,]+)§7(?:§m)?²(?:§b ✮ (?<silverbullPrice>[\\d,]+)§3²)?(?: .+)?");

    public abstract GameItem getAnnotation(ItemStack itemStack, StyledText name, int emeraldPrice);

    @Override
    public final ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        int emeraldPrice = parsePrice(itemStack);

        return getAnnotation(itemStack, name, emeraldPrice);
    }

    private int parsePrice(ItemStack itemStack) {
        // Optimization: Only check known line numbers for the price,
        //               this will save us a lot of memory
        ListTag loreTag = LoreUtils.getLoreTag(itemStack);

        // If the lore tag is null, there is no price
        if (loreTag == null) return 0;

        StyledText priceLoreLine = null;

        // Check the trade market price line first
        int lineToCheck = TRADE_MARKET_PRICE_LINE;
        if (loreTag.size() > lineToCheck + 1) {
            StyledText line = StyledText.fromJson(loreTag.getString(lineToCheck));

            if (line.matches(PRICE_STR) && loreTag.size() > lineToCheck + 1) {
                priceLoreLine = StyledText.fromJson(loreTag.getString(lineToCheck + 1));
            }
        }

        // If we didn't find the price line, check the normal price line
        if (priceLoreLine == null) {
            lineToCheck = loreTag.size() - 2;

            // Lore is too short to contain a price line
            if (lineToCheck < 0) return 0;

            StyledText line = StyledText.fromJson(loreTag.getString(lineToCheck));

            if (line.matches(PRICE_STR) && loreTag.size() > lineToCheck + 1) {
                priceLoreLine = StyledText.fromJson(loreTag.getString(lineToCheck + 1));
            }
        }

        // If we still didn't find the price line, return 0
        if (priceLoreLine == null) return 0;

        StyledText loreLine = LoreUtils.getLoreLine(itemStack, lineToCheck + 1);
        Matcher matcher = loreLine.getMatcher(PRICE_PATTERN);
        if (matcher.matches()) {
            // If there is a silverbull price, use that instead
            String priceStr = matcher.group("silverbullPrice");
            priceStr = priceStr == null ? matcher.group("price") : priceStr;

            return Integer.parseInt(priceStr.replaceAll(",", ""));
        }

        // There might be a non-emerald price, just return 0
        return 0;
    }
}
