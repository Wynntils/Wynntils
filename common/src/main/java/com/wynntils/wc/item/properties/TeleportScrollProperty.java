/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.item.properties;

import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wc.item.WynnItemStack;
import com.wynntils.wc.item.properties.type.TextOverlayProperty;
import com.wynntils.wc.item.parsers.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TeleportScrollProperty extends ItemProperty implements TextOverlayProperty {
    private static final CustomColor CITY_COLOR = CustomColor.fromChatFormatting(ChatFormatting.AQUA);
    private static final CustomColor DUNGEON_COLOR = CustomColor.fromChatFormatting(ChatFormatting.GOLD);

    private final TextOverlay textOverlay;

    public TeleportScrollProperty(WynnItemStack item) {
        super(item);

        // parse type & location
        CustomColor textColor = CITY_COLOR;
        String location = "";

        Component itemName = item.getHoverName();
        Matcher nameMatcher = WynnItemMatchers.teleportScrollNameMatcher(itemName);
        if (nameMatcher.find()) {
            location = ComponentUtils.stripFormatting(nameMatcher.group(1));

            if (location.equals("Dungeon")) {
                textColor = DUNGEON_COLOR;
                for (Component line : item.getOriginalTooltip()) {
                    Matcher locationMatcher = WynnItemMatchers.teleportScrollLocationMatcher(line);
                    if (!locationMatcher.matches()) continue;

                    // remove "the" to properly represent forgery scrolls
                    location = WynnUtils.normalizeBadString(locationMatcher.group(1))
                            .replace("the ", "");

                    location = Arrays.stream(location.split(" ", 2))
                            .map(s -> s.substring(0, 1))
                            .collect(Collectors.joining())
                            .toUpperCase(Locale.ROOT);

                    break;
                }
            } else {
                for (Component line : item.getOriginalTooltip()) {
                    Matcher locationMatcher = WynnItemMatchers.teleportScrollLocationMatcher(line);
                    if (!locationMatcher.matches()) continue;

                    location = locationMatcher.group(1).substring(0, 2);

                    break;
                }
            }
        }

        textOverlay = new TextOverlay(
                location,
                textColor,
                FontRenderer.TextAlignment.LEFT_ALIGNED,
                ItemTextOverlayFeature.teleportScrollShadow,
                0,
                0,
                1f);
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return ItemTextOverlayFeature.teleportScrollEnabled;
    }

    @Override
    public TextOverlay getTextOverlay() {
        return textOverlay;
    }
}
