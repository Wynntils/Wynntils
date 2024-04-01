/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.stats.type.StatType;
import java.util.Map;

public class SetInstance {
    private SetInfo setInfo;
    private Map<String, Boolean> activeItems;
    private int wynnCount;

    public SetInstance(SetInfo setInfo, Map<String, Boolean> activeItems, int wynnCount) {
        this.setInfo = setInfo;
        this.activeItems = activeItems;
        this.wynnCount = wynnCount;
    }

    public void update(SetInfo setInfo, Map<String, Boolean> activeItems, int wynnCount) {
        this.setInfo = setInfo;
        this.activeItems = activeItems;
        this.wynnCount = wynnCount;
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

    /**
     * wynnCount may be inaccurate when the user has two of the same ring equipped (wynncraft bug).
     * Use SetModel to determine the true count if necessary.
     * @return Wynncraft's possibly inaccurate count of how many items in the set are equipped
     */
    public int getWynnCount() {
        return wynnCount;
    }

    @Override
    public String toString() {
        return "SetInstance{" + "setInfo="
                + setInfo + ", activeItems="
                + activeItems + ", wynnCount="
                + wynnCount + '}';
    }
}
