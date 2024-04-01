/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.core.components.Models;
import com.wynntils.models.stats.type.StatType;
import java.util.Map;

// wynncraftCount can eventually be removed when Wynncraft fixes their double ring bug...
// Essentially if you have two of the same ring on, it's only counted once
public class SetInstance {
    private SetInfo setInfo;
    private Map<String, Boolean> activeItems;
    private int trueCount;
    private int wynnCount;

    public SetInstance(SetInfo setInfo, Map<String, Boolean> activeItems, int trueCount, int wynnCount) {
        this.setInfo = setInfo;
        this.activeItems = activeItems;
        this.trueCount = trueCount;
        this.wynnCount = wynnCount;
        Models.Set.updateAllSetInstances(this);
    }

    public void update(SetInfo setInfo, Map<String, Boolean> activeItems, int trueCount, int wynnCount) {
        this.setInfo = setInfo;
        this.activeItems = activeItems;
        this.trueCount = trueCount;
        this.wynnCount = wynnCount;
    }

    public Map<StatType, Integer> getTrueCountBonuses() {
        return setInfo.getBonusForItems(trueCount);
    }

    public Map<StatType, Integer> getWynnCountBonuses() {
        return setInfo.getBonusForItems(wynnCount);
    }

    public SetInfo getSetInfo() {
        return setInfo;
    }

    public Map<String, Boolean> getActiveItems() {
        return activeItems;
    }

    public int getTrueCount() {
        return trueCount;
    }

    public int getWynnCount() {
        return wynnCount;
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
