/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.gui.AbilityTreeItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class AbilityTreeAnnotator implements ItemAnnotator {
    // Deals with the ability tree button in the compass menu
    private static final StyledText ABILITY_TREE_NAME = StyledText.fromString("§b§lAbility Tree");
    private static final Pattern ABILITY_POINTS_PATTERN = Pattern.compile("^§3✦ Unused Points: §f(\\d+)$");

    // Deals with the reset button in the ability tree screen
    private static final StyledText INTERNAL_ABILITY_TREE_NAME = StyledText.fromString("§3§lAbility Points");
    // Test suite: https://regexr.com/7h12b
    private static final Pattern INTERNAL_ABILITY_POINTS_PATTERN =
            Pattern.compile("^§b✦ Available Points: §f(\\d+)§7/\\d+$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (name.equals(ABILITY_TREE_NAME)) {
            Matcher matcher = LoreUtils.matchLoreLine(itemStack, 3, ABILITY_POINTS_PATTERN);
            if (!matcher.matches()) return null;

            int count = Integer.parseInt(matcher.group(1));
            return new AbilityTreeItem(count);
        } else if (name.equals(INTERNAL_ABILITY_TREE_NAME)) {
            Matcher matcher = LoreUtils.matchLoreLine(itemStack, 3, INTERNAL_ABILITY_POINTS_PATTERN);
            if (!matcher.matches()) return null;

            int count = Integer.parseInt(matcher.group(1));
            return new AbilityTreeItem(count);
        } else {
            return null;
        }
    }
}
