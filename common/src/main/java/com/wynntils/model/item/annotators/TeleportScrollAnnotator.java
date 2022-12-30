/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.model.item.game.TeleportScrollItem;
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

    public static Matcher teleportScrollNameMatcher(Component text) {
        return TELEPORT_SCROLL_PATTERN.matcher(WynnUtils.normalizeBadString(ComponentUtils.getCoded(text)));
    }

    public static Matcher teleportScrollLocationMatcher(Component text) {
        return TELEPORT_LOCATION_PATTERN.matcher(WynnUtils.normalizeBadString(text.getString()));
    }

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        boolean dungeon = false;
        String destination = "";

        Component itemName = itemStack.getHoverName();
        Matcher nameMatcher = teleportScrollNameMatcher(itemName);
        if (!nameMatcher.matches()) return null;

        String scrollName = ComponentUtils.stripFormatting(nameMatcher.group(1));

        if (scrollName.equals("Dungeon")) {
            dungeon = true;
            for (Component line : itemStack.getTooltipLines(null, TooltipFlag.NORMAL)) {
                Matcher locationMatcher = teleportScrollLocationMatcher(line);
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
