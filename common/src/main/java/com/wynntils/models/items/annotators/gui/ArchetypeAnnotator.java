/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.gui.ArchetypeItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class ArchetypeAnnotator implements ItemAnnotator {
    // Test suite: https://regexr.com/7h12h
    private static final Pattern ARCHETYPE_NAME = Pattern.compile("^§([a-r0-9])§l[A-Za-z ]+ Archetype$");
    // Test suite: https://regexr.com/7h133
    private static final Pattern ARCHETYPE_PATTERN = Pattern.compile("^§a✔ §7Unlocked Abilities: §f(\\d+)§7/1[56]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher nameMatcher = name.getMatcher(ARCHETYPE_NAME);
        if (!nameMatcher.matches()) return null;

        // certain archetypes like Shadestepper have an extra line of description
        int matchLine = LoreUtils.getLoreLine(itemStack, 5) == StyledText.EMPTY ? 6 : 5;
        Matcher loreMatcher = LoreUtils.matchLoreLine(itemStack, matchLine, ARCHETYPE_PATTERN);
        if (!loreMatcher.matches()) return null;

        int count = Integer.parseInt(loreMatcher.group(1));
        char colorCode = nameMatcher.group(1).charAt(0);
        return new ArchetypeItem(count, colorCode);
    }
}
