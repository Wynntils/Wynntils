/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText2;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.gui.AbilityTreeItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class AbilityTreeAnnotator implements ItemAnnotator {
    private static final StyledText2 ABILITY_TREE_NAME = StyledText2.of("§b§lAbility Tree");
    private static final Pattern ABILITY_POINTS_PATTERN = Pattern.compile("^§3✦ Unused Points: §r§f(\\d+)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText2 name) {
        if (!name.equals(ABILITY_TREE_NAME)) return null;

        Matcher matcher = LoreUtils.matchLoreLine(itemStack, 3, ABILITY_POINTS_PATTERN);
        if (!matcher.matches()) return null;

        int count = Integer.parseInt(matcher.group(1));
        return new AbilityTreeItem(count);
    }
}
