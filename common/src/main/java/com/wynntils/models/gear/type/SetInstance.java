/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.stats.type.StatType;

import java.util.List;
import java.util.Map;

// wynncraftCount can eventually be removed when Wynncraft fixes their double ring bug...
// Essentially if you have two of the same ring on, it's only counted once
public class SetInstance {
    private final SetInfo setInfo;
    private int wynncraftCount;
    private int trueCount;
    private List<SetInstance> setInstances; // List of other instances of the same set

    public SetInstance(SetInfo setInfo, int wynncraftCount, int trueCount, List<SetInstance> setInstances) {
        this.setInfo = setInfo;
        this.wynncraftCount = wynncraftCount;
        this.trueCount = trueCount;
        this.setInstances = setInstances;
    }

    public SetInfo getSetInfo() {
        return setInfo;
    }

    public int getWynncraftCount() {
        return wynncraftCount;
    }

    public void setWynncraftCount(int wynncraftCount) {
        this.wynncraftCount = wynncraftCount;
    }

    public int getTrueCount() {
        return trueCount;
    }

    public void setTrueCount(int trueCount) {
        this.trueCount = trueCount;
    }

    public Map<StatType, Integer> getTrueCountBonuses() {
        return setInfo.getBonusForItems(trueCount);
    }

    public List<SetInstance> getSetInstances() {
        return setInstances;
    }

    public void setSetInstances(List<SetInstance> setInstances) {
        this.setInstances = setInstances;
    }

    @Override
    public String toString() {
        // avoid stack overflow
        StringBuilder setInstancesString = new StringBuilder("[");
        for (SetInstance instance : setInstances) {
            setInstancesString.append("{wynncraftCount=").append(instance.getWynncraftCount())
                    .append(", trueCount=").append(instance.getTrueCount()).append("}, ");
        }
        if (!setInstances.isEmpty()) {
            setInstancesString.setLength(setInstancesString.length() - 2); // Remove the last comma and space
        }
        setInstancesString.append("]");

        return "SetInstance{" +
                "setInfo=" + setInfo +
                ", wynncraftCount=" + wynncraftCount +
                ", trueCount=" + trueCount +
                ", setInstances=" + setInstancesString +
                '}';
    }
}
