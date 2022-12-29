/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.game;

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
}
