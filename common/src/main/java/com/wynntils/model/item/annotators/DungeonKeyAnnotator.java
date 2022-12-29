/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.model.item.game.DungeonKeyItem;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class DungeonKeyAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        Matcher keyMatcher = WynnItemMatchers.dungeonKeyNameMatcher(itemStack.getHoverName());
        if (!keyMatcher.matches()) return null;

        if (!verifyDungeonKey(itemStack)) return null;

        String name = keyMatcher.group();

        String dungeon = Arrays.stream(keyMatcher.group(1).split(" ", 2))
                .map(s -> s.substring(0, 1))
                .collect(Collectors.joining());

        boolean corrupted = name.contains("Corrupted") || name.contains("Broken");

        return new DungeonKeyItem(dungeon, corrupted);
    }

    private boolean verifyDungeonKey(ItemStack itemStack) {
        for (Component line : ItemUtils.getTooltipLines(itemStack)) {
            // check lore to avoid matching misc. key items
            if (line.getString().contains("Dungeon Info")) return true;
            if (line.getString().contains("Corrupted Dungeon Key")) return true;
        }
        return false;
    }
}
