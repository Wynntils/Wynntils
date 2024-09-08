/*
 * Copyright © Wynntils 2022-2024.
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
    private static final Pattern COMPASS_ABILITY_POINTS_PATTERN = Pattern.compile("^§3✦ Unused Points: §f(\\d+)$");

    // Deals with the reset button in the ability tree screen
    private static final StyledText TREE_ABILITY_POINTS_NAME = StyledText.fromString("§#82eff4ff§lAbility Points");
    // Test in AbilityTreeAnnotator_TREE_ABILITY_POINTS_PATTERN
    private static final Pattern TREE_ABILITY_POINTS_PATTERN =
            Pattern.compile("^§b✦ Available Points: §f(\\d+)§7/\\d+$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (name.equals(COMPASS_ABILITY_POINTS_NAME)) {
            Matcher matcher = LoreUtils.matchLoreLine(itemStack, 3, COMPASS_ABILITY_POINTS_PATTERN);
            if (!matcher.matches()) return null;

            int count = Integer.parseInt(matcher.group(1));
            return new AbilityTreeItem(count);
        } else if (name.equals(TREE_ABILITY_POINTS_NAME)) {
            Matcher matcher = LoreUtils.matchLoreLine(itemStack, 3, TREE_ABILITY_POINTS_PATTERN);
            if (!matcher.matches()) return null;

            int count = Integer.parseInt(matcher.group(1));
            return new AbilityTreeItem(count);
        } else {
            return null;
        }
    }
}
