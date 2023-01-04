/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;
import com.wynntils.wynn.utils.WynnItemMatchers;
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
                new TextRenderTask(
                        dungeon,
                        TextRenderSetting.DEFAULT
                                .withCustomColor(textColor)
                                .withTextShadow(ItemTextOverlayFeature.INSTANCE.dungeonKeyShadow)),
                -1,
                1,
                1f);
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return ItemTextOverlayFeature.INSTANCE.dungeonKeyEnabled;
    }

    @Override
    public TextOverlay getTextOverlay() {
        return textOverlay;
    }
}
