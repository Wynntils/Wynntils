/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.stats.type.StatType;
import java.util.Map;

// wynncraftCount can eventually be removed when Wynncraft fixes their double ring bug...
// Essentially if you have two of the same ring on, it's only counted once
public record SetInstance(SetInfo setInfo, Map<String, Boolean> activeItems, int trueCount, int wynnCount) {
    public Map<StatType, Integer> getTrueCountBonuses() {
        return setInfo.getBonusForItems(trueCount);
    }

    public Map<StatType, Integer> getWynnCountBonuses() {
        return setInfo.getBonusForItems(wynnCount);
    }

    @Override
    public String toString() {
        return "SetInstance{" + "setInfo="
                + setInfo + ", activeItems="
                + activeItems + ", trueCount="
                + trueCount + ", wynnCount="
                + wynnCount + '}';
    }
}
