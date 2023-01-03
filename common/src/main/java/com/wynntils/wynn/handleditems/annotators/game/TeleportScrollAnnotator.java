/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.handleditems.items.game.TeleportScrollItem;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class TeleportScrollAnnotator implements ItemAnnotator {
    private static final Pattern TELEPORT_SCROLL_PATTERN = Pattern.compile(".*§b(.*) Teleport Scroll");
    private static final Pattern TELEPORT_LOCATION_PATTERN = Pattern.compile("- Teleports to: (.*)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher nameMatcher = TELEPORT_SCROLL_PATTERN.matcher(name);
        if (!nameMatcher.matches()) return null;

        boolean dungeon = false;
        String destination = "";

        String scrollName = ComponentUtils.stripFormatting(nameMatcher.group(1));

        if (scrollName.equals("Dungeon")) {
            dungeon = true;
            for (Component line : itemStack.getTooltipLines(null, TooltipFlag.NORMAL)) {
                Matcher locationMatcher =
                        TELEPORT_LOCATION_PATTERN.matcher(WynnUtils.normalizeBadString(line.getString()));
                if (!locationMatcher.matches()) continue;

                // remove "the" to properly represent forgery scrolls
                destination =
                        WynnUtils.normalizeBadString(locationMatcher.group(1)).replace("the ", "");

                destination = Arrays.stream(destination.split(" ", 2))
                        .map(s -> s.substring(0, 1))
                        .collect(Collectors.joining())
                        .toUpperCase(Locale.ROOT);

                break;
            }
        } else {
            destination = scrollName.substring(0, 2);
        }

        return new TeleportScrollItem(destination, dungeon);
    }
}
