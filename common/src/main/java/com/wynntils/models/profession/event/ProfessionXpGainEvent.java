/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import com.wynntils.models.profession.type.ProfessionType;

public final class ProfessionXpGainEvent extends BaseEvent implements CancelRequestable {
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
