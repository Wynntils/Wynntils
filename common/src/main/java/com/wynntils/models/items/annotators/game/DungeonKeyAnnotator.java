/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.DungeonKeyItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;

public final class DungeonKeyAnnotator implements ItemAnnotator {
    private static final Pattern DUNGEON_KEY_PATTERN =
            Pattern.compile("^(?:§[46])*(?:Broken )?(?:Corrupted )?(.+) Key$");

    private static final Pattern LORE_PATTERN = Pattern.compile("§7(Grants access to the|Use this item at the)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher keyMatcher = name.getMatcher(DUNGEON_KEY_PATTERN);
        if (!keyMatcher.matches()) return null;

        if (!verifyDungeonKey(itemStack, name)) return null;

        String dungeonName = keyMatcher.group();

        String dungeon = Arrays.stream(keyMatcher.group(1).split(" ", 2))
                .map(s -> s.substring(0, 1))
                .collect(Collectors.joining());

        boolean corrupted = dungeonName.contains("Corrupted") || dungeonName.contains("Broken");

        return new DungeonKeyItem(dungeon, corrupted);
    }

    private boolean verifyDungeonKey(ItemStack itemStack, StyledText name) {
        if (name.startsWith("Broken")) return true;

        Matcher matcher = LoreUtils.matchLoreLine(itemStack, 0, LORE_PATTERN);
        return matcher.matches();
    }
}
