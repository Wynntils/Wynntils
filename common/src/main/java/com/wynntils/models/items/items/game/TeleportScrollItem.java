/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.TargetedItemProperty;

public class TeleportScrollItem extends GameItem implements TargetedItemProperty {
    private final String destination;
    private final int remainingCharges;

    public TeleportScrollItem(String destination, int remainingCharges) {
        this.destination = destination;
        this.remainingCharges = remainingCharges;
    }

    public String getDestination() {
        return destination;
    }

    public int getRemainingCharges() {
        return remainingCharges;
    }

    @Override
    public String getTarget() {
        return destination;
    }

    @Override
    public String toString() {
        return "TeleportScrollItem{" + "destination='"
                + destination + '\'' + ", remainingCharges="
                + remainingCharges + '}';
    }
}
