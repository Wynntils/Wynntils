/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.DungeonKeyItem;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;

public final class DungeonKeyAnnotator implements ItemAnnotator {
    private static final Pattern DUNGEON_KEY_PATTERN =
            Pattern.compile("^(?:§[46])*(?:Broken )?(?:Corrupted )?(.+) Key$");

    private static final String[] DUNGEONS = {
        "Decrepit Sewers",
        "Infested Pit",
        "Lost Sanctuary",
        "Underworld Crypt",
        "Timelost Sanctum",
        "Sand-Swept Tomb",
        "Ice Barrows",
        "Undergrowth Ruins",
        "Galleon's Graveyard",
        "Fallen Factory",
        "Eldritch Outlook"
    };

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher keyMatcher = name.getMatcher(DUNGEON_KEY_PATTERN);
        if (!keyMatcher.matches()) return null;

        if (!verifyDungeonKey(keyMatcher.group(1))) return null;

        String dungeon = Arrays.stream(keyMatcher.group(1).split(" ", 2))
                .map(s -> s.substring(0, 1))
                .collect(Collectors.joining());

        String itemName = name.getString();
        boolean corrupted = itemName.contains("Corrupted") || itemName.contains("Broken");

        return new DungeonKeyItem(dungeon, corrupted);
    }

    private boolean verifyDungeonKey(String dungeonName) {
        for (String dungeon : DUNGEONS) {
            if (dungeonName.equals(dungeon)) {
                return true;
            }
        }

        return false;
    }
}
