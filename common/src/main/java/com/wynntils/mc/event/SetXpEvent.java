/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public class SetXpEvent extends Event {
    private final float experienceProgress;
    private final int totalExperience;
    private final int experienceLevel;

    public SetXpEvent(float experienceProgress, int totalExperience, int experienceLevel) {
        this.experienceProgress = experienceProgress;
        this.totalExperience = totalExperience;
        this.experienceLevel = experienceLevel;
    }

    public float getExperienceProgress() {
        return experienceProgress;
    }

    public int getExperienceLevel() {
        return experienceLevel;
    }

    public int getTotalExperience() {
        return totalExperience;
    }
}
