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

    // The ALT is for when we have tree manipulation and can reset the tree
    private static final StyledText TREE_ABILITY_POINTS_NAME_ALT = StyledText.fromString("§e§lAbility Points");
    private static final Pattern TREE_ABILITY_POINTS_RESET_PATTERN =
            Pattern.compile("§eShift Click to reset your tree");

    // Test in AbilityTreeAnnotator_ABILITY_POINTS_PATTERN
    private static final Pattern ABILITY_POINTS_PATTERN =
            Pattern.compile("§b✦ Available Points: §(?:#a0c84bff|f)(\\d+)§7\\/(\\d+)");
    private static final Pattern LOANED_ABILITY_POINTS_PATTERN =
            Pattern.compile("§#a0c84bff(\\d+) early points? from §..");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        boolean isCompass = name.equals(COMPASS_ABILITY_POINTS_NAME);
        boolean isTreeAlt = name.equals(TREE_ABILITY_POINTS_NAME_ALT);
        boolean isTree = isTreeAlt || name.equals(TREE_ABILITY_POINTS_NAME);
        if (!isCompass && !isTree) return null;

        Matcher matcher = LoreUtils.matchLoreLine(itemStack, 3, ABILITY_POINTS_PATTERN);
        if (!matcher.matches()) return null;

        int count = Integer.parseInt(matcher.group(1)); // available points
        int totalPoints = Integer.parseInt(matcher.group(2));
        int loanedPoints = 0;
        boolean canReset = false;

        for (StyledText styledText : LoreUtils.getLore(itemStack)) {
            String line = styledText.getString();

            if (loanedPoints == 0) {
                Matcher loanMatcher = LOANED_ABILITY_POINTS_PATTERN.matcher(line);
                if (loanMatcher.matches()) {
                    loanedPoints = Integer.parseInt(loanMatcher.group(1));
                }
            }

            if (isTreeAlt
                    && !canReset
                    && TREE_ABILITY_POINTS_RESET_PATTERN.matcher(line).matches()) {
                canReset = true;
            }
        }

        return new AbilityTreeItem(count, totalPoints, loanedPoints, canReset);
    }
}
