/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.activities.type.Dungeon;
import com.wynntils.models.items.items.game.DungeonKeyItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class DungeonKeyAnnotator implements GameItemAnnotator {
    private static final Pattern DUNGEON_KEY_PATTERN =
            Pattern.compile("^(?:§[46])*(?<broken>Broken )?(?<corrupted>Corrupted )?(?<dungeonName>.+) Key$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher keyMatcher = name.getMatcher(DUNGEON_KEY_PATTERN);
        if (!keyMatcher.matches()) return null;

        Dungeon dungeon = Dungeon.fromName(keyMatcher.group("dungeonName"));
        if (dungeon == null) return null;

        boolean broken = keyMatcher.group("broken") != null;
        boolean corrupted = keyMatcher.group("corrupted") != null;

        return new DungeonKeyItem(dungeon, broken, corrupted);
    }
}
