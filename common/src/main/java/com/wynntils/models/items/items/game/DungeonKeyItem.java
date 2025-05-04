/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.activities.type.Dungeon;
import com.wynntils.models.items.properties.TargetedItemProperty;

public class DungeonKeyItem extends GameItem implements TargetedItemProperty {
    private final Dungeon dungeon;
    private final boolean corrupted;

    public DungeonKeyItem(Dungeon dungeon, boolean corrupted) {
        this.dungeon = dungeon;
        this.corrupted = corrupted;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public boolean isCorrupted() {
        return corrupted;
    }

    @Override
    public String getTarget() {
        return dungeon.getName();
    }

    @Override
    public String toString() {
        return "DungeonKeyItem{" + "dungeon='" + dungeon + '\'' + ", corrupted=" + corrupted + '}';
    }
}
