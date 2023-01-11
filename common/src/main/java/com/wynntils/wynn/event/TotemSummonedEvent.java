/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import net.minecraft.world.entity.decoration.ArmorStand;

public class TotemSummonedEvent extends TotemEvent {
    private final ArmorStand totemEntity;

    public TotemSummonedEvent(int totemNumber, ArmorStand totemEntity) {
        super(totemNumber);
        this.totemEntity = totemEntity;
    }

    public ArmorStand getTotemEntity() {
        return totemEntity;
    }
}
