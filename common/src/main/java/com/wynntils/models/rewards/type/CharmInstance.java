/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.models.gear.type.GearInstanceRequirements;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.List;
import java.util.Optional;

public final class CharmInstance {
    private GearInstanceRequirements gearInstanceRequirements;
    private int rerolls;
    private List<StatActualValue> identifications;
    private Optional<Float> overallQuality;

    public CharmInstance() {
        this(0, List.of(), Optional.empty(), null);
    }

    public CharmInstance(
            int rerolls,
            List<StatActualValue> identifications,
            Optional<Float> overallQuality,
            GearInstanceRequirements gearInstanceRequirements) {
        this.rerolls = rerolls;
        this.identifications = List.copyOf(identifications);
        this.overallQuality = overallQuality;
        this.gearInstanceRequirements = gearInstanceRequirements;
    }

    public GearInstanceRequirements gearInstanceRequirements() {
        return gearInstanceRequirements;
    }

    public int rerolls() {
        return rerolls;
    }

    public List<StatActualValue> identifications() {
        return identifications;
    }

    public Optional<Float> overallQuality() {
        return overallQuality;
    }

    public void updateStats(
            int rerolls,
            CharmInfo charmInfo,
            List<StatActualValue> identifications,
            GearInstanceRequirements gearInstanceRequirements) {
        this.gearInstanceRequirements = gearInstanceRequirements;
        this.rerolls = rerolls;
        this.identifications = List.copyOf(identifications);
        this.overallQuality = StatCalculator.calculateOverallQuality(
                charmInfo.name(), charmInfo.getPossibleValueList(), identifications);
    }

    public static CharmInstance create(
            int rerolls,
            CharmInfo charmInfo,
            List<StatActualValue> identifications,
            GearInstanceRequirements gearInstanceRequirements) {
        return new CharmInstance(
                rerolls,
                identifications,
                StatCalculator.calculateOverallQuality(
                        charmInfo.name(), charmInfo.getPossibleValueList(), identifications),
                gearInstanceRequirements);
    }

    public boolean hasOverallValue() {
        return overallQuality.isPresent();
    }

    public float getOverallPercentage() {
        return overallQuality.orElse(0.0f);
    }

    public boolean isPerfect() {
        return overallQuality.orElse(0.0f) >= 100.0f;
    }

    public boolean isDefective() {
        return overallQuality.isPresent() && overallQuality.orElse(0.0f) <= 0.0f;
    }
}
