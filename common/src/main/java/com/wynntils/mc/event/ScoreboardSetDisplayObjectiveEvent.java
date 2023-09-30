/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.scores.DisplaySlot;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ScoreboardSetDisplayObjectiveEvent extends Event {
    private final DisplaySlot slot;
    private final String objectiveName;

    public ScoreboardSetDisplayObjectiveEvent(DisplaySlot slot, String objectiveName) {
        this.slot = slot;
        this.objectiveName = objectiveName;
    }

    public DisplaySlot getSlot() {
        return slot;
    }

    public String getObjectiveName() {
        return objectiveName;
    }
}
