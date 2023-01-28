/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.type;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatType;
import java.util.List;
import net.minecraft.network.chat.Component;

public class GearInstance {
    private final List<StatActualValue> identifications;
    private final List<Powder> powders;
    private final int rerolls;
    private final List<Component> setBonus;
    private final boolean isPerfect;
    private final boolean isDefective;
    private final float overallPercentage;
    private final boolean hasVariableIds;

    public GearInstance(
            List<StatActualValue> identifications, List<Powder> powders, int rerolls, List<Component> setBonus) {
        this.identifications = identifications;
        this.powders = powders;
        this.rerolls = rerolls;
        this.setBonus = setBonus;

        /*
        DoubleSummaryStatistics percents = identifications.stream()
                .filter(Predicate.not(GearIdentificationContainer::isFixed))
                .mapToDouble(GearIdentificationContainer::percent)
                .summaryStatistics();
        overallPercentage = (float) percents.getAverage();
        if (percents.getCount() > 0) {
            // Only claim it is perfect/defective if we do have some non-fixed identifications
            isPerfect = overallPercentage >= 100f;
            isDefective = overallPercentage <= 0f;
            hasVariableIds = true;
        } else {
            isPerfect = false;
            isDefective = false;
            hasVariableIds = false;
        }

         */

        isPerfect = false;
        isDefective = false;
        hasVariableIds = true;
        overallPercentage = 33.1f;
    }

    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    public List<Powder> getPowders() {
        return powders;
    }

    public int getRerolls() {
        return rerolls;
    }

    public List<Component> getSetBonus() {
        return setBonus;
    }

    @Override
    public String toString() {
        return "GearItem{identifications="
                + identifications + ", powders="
                + powders + ", rerolls="
                + rerolls + ", setBonus="
                + setBonus + '}';
    }

    public boolean hasVariableIds() {
        return hasVariableIds;
    }

    public float getOverallPercentage() {
        return overallPercentage;
    }

    public boolean isPerfect() {
        return isPerfect;
    }

    public boolean isDefective() {
        return isDefective;
    }

    public StatActualValue getActualValue(StatType statType) {
        return identifications.stream()
                .filter(s -> s.stat().equals(statType))
                .findFirst()
                .orElse(null);
    }
}
