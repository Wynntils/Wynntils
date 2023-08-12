/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.TeleportScrollItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;

public final class TeleportScrollAnnotator implements ItemAnnotator {
    private static final Pattern TELEPORT_SCROLL_PATTERN = Pattern.compile("^§b(.*) Teleport Scroll$");
    private static final Pattern TELEPORT_LOCATION_PATTERN = Pattern.compile("§3- §7Teleports to: §f(.*)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher nameMatcher = name.getMatcher(TELEPORT_SCROLL_PATTERN);
        if (!nameMatcher.matches()) return null;

        String scrollName = nameMatcher.group(1);

        if (scrollName.equals("Dungeon")) {
            Matcher dungeonMatcher = LoreUtils.matchLoreLine(itemStack, 3, TELEPORT_LOCATION_PATTERN);
            if (!dungeonMatcher.matches()) return null;

            // remove "the" to properly represent forgery scrolls
            String destination =
                    WynnUtils.normalizeBadString(dungeonMatcher.group(1)).replace("the ", "");

            destination = Arrays.stream(destination.split(" ", 2))
                    .map(s -> s.substring(0, 1))
                    .collect(Collectors.joining())
                    .toUpperCase(Locale.ROOT);
            return new TeleportScrollItem(destination, true);
        } else {
            String destination = scrollName.substring(0, 2);
            return new TeleportScrollItem(destination, false);
        }
    }
}
