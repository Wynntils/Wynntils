/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.TargetedItemProperty;

public class TeleportScrollItem extends GameItem implements TargetedItemProperty {
    private final String destination;
    private final boolean dungeon;

    public TeleportScrollItem(String destination, boolean dungeon) {
        this.destination = destination;
        this.dungeon = dungeon;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isDungeon() {
        return dungeon;
    }

    @Override
    public String getTarget() {
        return destination;
    }

    @Override
    public String toString() {
        return "TeleportScrollItem{" + "destination='" + destination + '\'' + ", dungeon=" + dungeon + '}';
    }
}
