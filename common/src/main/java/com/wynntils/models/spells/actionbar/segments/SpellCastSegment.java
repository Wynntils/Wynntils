/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.spells.type.SpellType;

public class SpellCastSegment extends ActionBarSegment {
    private final SpellType spellType;
    private final int manaCost;
    private final int healthCost;

    public SpellCastSegment(
            String segmentText, int startIndex, int endIndex, SpellType spellType, int manaCost, int healthCost) {
        super(segmentText, startIndex, endIndex);

        this.spellType = spellType;
        this.manaCost = manaCost;
        this.healthCost = healthCost;
    }

    public SpellType getSpellType() {
        return spellType;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getHealthCost() {
        return healthCost;
    }

    @Override
    public String toString() {
        return "SpellCastSegment{" + "healthCost="
                + healthCost + ", spellType="
                + spellType + ", manaCost="
                + manaCost + ", segmentText='"
                + segmentText + '\'' + '}';
    }
}
