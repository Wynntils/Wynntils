/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.event;

import com.wynntils.models.profession.type.ProfessionType;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ProfessionXpGainEvent extends Event {
    private final ProfessionType profession;
    private final float gainedXpRaw;
    private final float currentXpPercentage;

    public ProfessionXpGainEvent(ProfessionType profession, float gainedXpRaw, float currentXpPercentage) {
        this.profession = profession;
        this.gainedXpRaw = gainedXpRaw;
        this.currentXpPercentage = currentXpPercentage;
    }

    public ProfessionType getProfession() {
        return profession;
    }

    public float getCurrentXpPercentage() {
        return currentXpPercentage;
    }

    public float getGainedXpRaw() {
        return gainedXpRaw;
    }
}
