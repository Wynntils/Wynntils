/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.item.properties;

import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.wc.item.WynnItemStack;
import com.wynntils.wc.item.parsers.WynnItemMatchers;
import com.wynntils.wc.item.properties.type.TextOverlayProperty;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;

public class DungeonKeyProperty extends ItemProperty implements TextOverlayProperty {
    private static final CustomColor STANDARD_COLOR = CustomColor.fromChatFormatting(ChatFormatting.GOLD);
    private static final CustomColor CORRUPTED_COLOR = CustomColor.fromChatFormatting(ChatFormatting.DARK_RED);

    private final TextOverlay textOverlay;

    public DungeonKeyProperty(WynnItemStack item) {
        super(item);

        // parse dungeon name & type
        CustomColor textColor = STANDARD_COLOR;
        String dungeon = "";

        Matcher keyMatcher = WynnItemMatchers.dungeonKeyNameMatcher(item.getHoverName());
        if (keyMatcher.matches()) {
            String name = keyMatcher.group();
            if (name.contains("Corrupted") || name.contains("Broken")) {
                textColor = CORRUPTED_COLOR;
            }

            dungeon = Arrays.stream(keyMatcher.group(1).split(" ", 2))
                    .map(s -> s.substring(0, 1))
                    .collect(Collectors.joining());
        }

        textOverlay = new TextOverlay(
                dungeon,
                textColor,
                FontRenderer.TextAlignment.LEFT_ALIGNED,
                ItemTextOverlayFeature.dungeonKeyShadow,
                -1,
                1,
                1f);
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return ItemTextOverlayFeature.dungeonKeyEnabled;
    }

    @Override
    public TextOverlay getTextOverlay() {
        return textOverlay;
    }
}
