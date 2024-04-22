/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.horse.type.HorseTier;
import com.wynntils.models.items.items.game.HorseItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HorseAnnotator implements GameItemAnnotator {
    private static final Pattern HORSE_PATTERN = Pattern.compile("^§f(.*) Horse$");
    private static final Pattern HORSE_TIER_PATTERN = Pattern.compile("^§7Tier (\\d)$");
    private static final Pattern HORSE_LEVEL_PATTERN = Pattern.compile("^§6Speed: (\\d+)/(\\d+)$");
    private static final Pattern HORSE_XP_PATTERN = Pattern.compile("^§bXp: (\\d+)/100$");
    private static final Pattern HORSE_NAME_PATTERN = Pattern.compile("^§7Name: (.+)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.SADDLE) return null;
        Matcher matcher = name.getMatcher(HORSE_PATTERN);
        if (!matcher.matches()) return null;

        Matcher tierMatcher = LoreUtils.matchLoreLine(itemStack, 0, HORSE_TIER_PATTERN);

        // The lore may be missing if the item is on the trade market
        if (!tierMatcher.matches()) {
            return new HorseItem(HorseTier.fromName(matcher.group(1)), CappedValue.EMPTY, CappedValue.EMPTY, null);
        }
        HorseTier tier = HorseTier.fromNumeral(Integer.parseInt(tierMatcher.group(1)));

        Matcher levelMatcher = LoreUtils.matchLoreLine(itemStack, 1, HORSE_LEVEL_PATTERN);
        if (!levelMatcher.matches()) return null;
        int level = Integer.parseInt(levelMatcher.group(1));
        int maxLevel = Integer.parseInt(levelMatcher.group(2));

        Matcher xpMatcher = LoreUtils.matchLoreLine(itemStack, 4, HORSE_XP_PATTERN);
        if (!xpMatcher.matches()) return null;
        int xp = Integer.parseInt(xpMatcher.group(1));

        Matcher nameMatcher = LoreUtils.matchLoreLine(itemStack, 5, HORSE_NAME_PATTERN);
        String horseName = nameMatcher.matches() ? nameMatcher.group(1) : null;

        return new HorseItem(tier, new CappedValue(level, maxLevel), new CappedValue(xp, 100), horseName);
    }
}
