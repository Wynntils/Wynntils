/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ScoreboardSetDisplayObjectiveEvent extends Event {
    private final int slot;
    private final String objectiveName;

    public ScoreboardSetDisplayObjectiveEvent(int slot, String objectiveName) {
        this.slot = slot;
        this.objectiveName = objectiveName;
    }

    public int getSlot() {
        return slot;
    }

    public String getObjectiveName() {
        return objectiveName;
    }
}
