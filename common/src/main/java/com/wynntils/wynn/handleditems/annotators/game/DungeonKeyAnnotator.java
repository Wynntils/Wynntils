/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.LoreUtils;
import com.wynntils.wynn.handleditems.items.game.DungeonKeyItem;
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
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher keyMatcher = DUNGEON_KEY_PATTERN.matcher(name);
        if (!keyMatcher.matches()) return null;

        if (!verifyDungeonKey(itemStack, name)) return null;

        String dungeonName = keyMatcher.group();

        String dungeon = Arrays.stream(keyMatcher.group(1).split(" ", 2))
                .map(s -> s.substring(0, 1))
                .collect(Collectors.joining());

        boolean corrupted = dungeonName.contains("Corrupted") || dungeonName.contains("Broken");

        return new DungeonKeyItem(dungeon, corrupted);
    }

    private boolean verifyDungeonKey(ItemStack itemStack, String name) {
        if (name.startsWith("Broken")) return true;

        Matcher matcher = LoreUtils.matchLoreLine(itemStack, 0, LORE_PATTERN);
        return matcher.matches();
    }
}
