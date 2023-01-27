/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;

public class GearInstance {
    private final List<StatActualValue> identifications;
    private final List<GearIdentificationContainer> idContainers;
    private final List<Powder> powders;
    private final int rerolls;
    private final List<Component> setBonus;
    private final boolean isPerfect;
    private final boolean isDefective;
    private final float overallPercentage;
    private final boolean hasVariableIds;

    public GearInstance(
            List<StatActualValue> identifications,
            List<GearIdentificationContainer> idContainers,
            List<Powder> powders,
            int rerolls,
            List<Component> setBonus) {
        this.identifications = identifications;
        this.idContainers = idContainers;
        this.powders = powders;
        this.rerolls = rerolls;
        this.setBonus = setBonus;

        DoubleSummaryStatistics percents = idContainers.stream()
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
    }

    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    public List<GearIdentificationContainer> getIdContainers() {
        return idContainers;
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
}
