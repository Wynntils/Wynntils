/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

import com.wynntils.utils.type.CappedValue;

public class ManaTextSegment extends AbstractTextSegment {
    private final CappedValue mana;

    public ManaTextSegment(String segmentText, int startIndex, int endIndex, CappedValue mana) {
        super(segmentText, startIndex, endIndex);
        this.mana = mana;
    }

    public CappedValue getMana() {
        return mana;
    }

    @Override
    public String toString() {
        return "ManaTextSegment{" + "mana=" + mana + ", segmentText='" + segmentText + '\'' + '}';
    }
}
