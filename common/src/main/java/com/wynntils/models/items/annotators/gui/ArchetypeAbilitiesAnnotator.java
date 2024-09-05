/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.ArchetypeAbilitiesItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class ArchetypeAbilitiesAnnotator implements GuiItemAnnotator {
    // Test in ArchetypeAbilitiesAnnotator_ARCHETYPE_NAME
    private static final Pattern ARCHETYPE_NAME = Pattern.compile("^§#([a-f0-9]{8})§l[A-Za-z ]+ Archetype$");
    // Test in ArchetypeAbilitiesAnnotator_ARCHETYPE_PATTERN
    private static final Pattern ARCHETYPE_PATTERN = Pattern.compile("^§a✔ §7Unlocked Abilities: §f(\\d+)§7/(\\d+)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher nameMatcher = name.getMatcher(ARCHETYPE_NAME);
        if (!nameMatcher.matches()) return null;

        // certain archetypes like Shadestepper have an extra line of description
        Matcher loreMatcher = LoreUtils.matchLoreLine(itemStack, 5, ARCHETYPE_PATTERN);
        if (!loreMatcher.matches()) return null;

        int count = Integer.parseInt(loreMatcher.group(1));
        int max = Integer.parseInt(loreMatcher.group(2));
        return new ArchetypeAbilitiesItem(new CappedValue(count, max));
    }
}
