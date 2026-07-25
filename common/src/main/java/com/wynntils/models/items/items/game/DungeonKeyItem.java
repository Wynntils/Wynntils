/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.activities.type.Dungeon;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.items.properties.TargetedItemProperty;

public class DungeonKeyItem extends GameItem implements NamedItemProperty, TargetedItemProperty {
    private final Dungeon dungeon;
    private final boolean broken;
    private final boolean corrupted;

    public DungeonKeyItem(Dungeon dungeon, boolean broken, boolean corrupted) {
        this.dungeon = dungeon;
        this.broken = broken;
        this.corrupted = corrupted;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public boolean isBroken() {
        return broken;
    }

    public boolean isCorrupted() {
        return corrupted;
    }

    @Override
    public String getName() {
        return (broken ? "Broken " : "") + (corrupted ? "Corrupted " : "") + dungeon.getName() + " Key";
    }

    @Override
    public String getTarget() {
        return dungeon.getName();
    }

    @Override
    public String toString() {
        return "DungeonKeyItem{" + "dungeon=" + dungeon + ", broken=" + broken + ", corrupted=" + corrupted + '}';
    }
}
