/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.AbilityTreeItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class AbilityTreeAnnotator implements GuiItemAnnotator {
    // Deals with the ability tree button in the compass menu
    private static final StyledText COMPASS_ABILITY_POINTS_NAME = StyledText.fromString("§b§lAbility Tree");

    // Deals with the reset button in the ability tree screen
    private static final StyledText TREE_ABILITY_POINTS_NAME = StyledText.fromString("§#82eff4ff§lAbility Points");
    private static final StyledText TREE_ABILITY_POINTS_NAME_ALT = StyledText.fromString("§e§lAbility Points");
    private static final Pattern TREE_ABILITY_POINTS_RESET_PATTERN =
            Pattern.compile("§eShift Click to reset your tree");

    // Test in AbilityTreeAnnotator_ABILITY_POINTS_PATTERN
    private static final Pattern ABILITY_POINTS_PATTERN =
            Pattern.compile("§b✦ Available Points: §(?:#a0c84bff|f)(\\d+)§7\\/(\\d+)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (name.equals(COMPASS_ABILITY_POINTS_NAME)) {
            Matcher matcher = LoreUtils.matchLoreLine(itemStack, 3, ABILITY_POINTS_PATTERN);
            if (!matcher.matches()) return null;
            int count = Integer.parseInt(matcher.group(1)); // available points
            int totalPoints = Integer.parseInt(matcher.group(2));
            return new AbilityTreeItem(count, totalPoints, false);
        } else if (name.equals(TREE_ABILITY_POINTS_NAME) || name.equals(TREE_ABILITY_POINTS_NAME_ALT)) {
            Matcher matcher = LoreUtils.matchLoreLine(itemStack, 3, ABILITY_POINTS_PATTERN);
            if (!matcher.matches()) return null;
            int count = Integer.parseInt(matcher.group(1));
            int totalPoints = Integer.parseInt(matcher.group(2));

            boolean isReset = false;
            if (name.equals(TREE_ABILITY_POINTS_NAME_ALT)) {
                Matcher resetMatcher = LoreUtils.matchLoreLine(itemStack, 8, TREE_ABILITY_POINTS_RESET_PATTERN);
                isReset = resetMatcher.matches();
            }

            return new AbilityTreeItem(count, totalPoints, isReset);
        } else {
            return null;
        }
    }
}
