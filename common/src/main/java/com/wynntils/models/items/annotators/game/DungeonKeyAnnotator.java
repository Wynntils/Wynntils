/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.dungeon.type.Dungeon;
import com.wynntils.models.items.items.game.DungeonKeyItem;
import com.wynntils.models.items.items.game.GameItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class DungeonKeyAnnotator extends GameItemAnnotator {
    private static final Pattern DUNGEON_KEY_PATTERN =
            Pattern.compile("^(?:§[46])*(?:Broken )?(?:Corrupted )?(.+) Key$");

    @Override
    public GameItem getAnnotation(ItemStack itemStack, StyledText name, int emeraldPrice) {
        Matcher keyMatcher = name.getMatcher(DUNGEON_KEY_PATTERN);
        if (!keyMatcher.matches()) return null;

        Dungeon dungeon = Dungeon.fromName(keyMatcher.group(1));
        if (dungeon == null) return null;

        String itemName = name.getString();
        boolean corrupted = itemName.contains("Corrupted") || itemName.contains("Broken");

        return new DungeonKeyItem(emeraldPrice, dungeon, corrupted);
    }
}
