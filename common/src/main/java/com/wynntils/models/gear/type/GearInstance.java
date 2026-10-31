/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderSpecialTierInfo;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

public final class GearInstance {
    private List<StatActualValue> identifications;
    private List<Pair<Powder, Integer>> powders;
    private Optional<PowderSpecialTierInfo> powderSpecial = Optional.empty();
    private int rerolls;
    private Optional<Float> overallQuality;
    private Optional<SetInstance> setInstance = Optional.empty();

    public GearInstance() {
        this(List.of(), List.of(), 0, Optional.empty());
    }

    public GearInstance(
            List<StatActualValue> identifications,
            List<Pair<Powder, Integer>> powders,
            int rerolls,
            Optional<Float> overallQuality) {
        this.identifications = List.copyOf(identifications);
        this.powders = List.copyOf(powders);
        this.rerolls = rerolls;
        this.overallQuality = overallQuality;
    }

    public List<StatActualValue> identifications() {
        return identifications;
    }

    public List<Pair<Powder, Integer>> powders() {
        return powders;
    }

    public int rerolls() {
        return rerolls;
    }

    public Optional<Float> overallQuality() {
        return overallQuality;
    }

    public Optional<SetInstance> getSetInstance() {
        return setInstance;
    }

    public void setSetInstance(Optional<SetInstance> setInstance) {
        this.setInstance = setInstance;
    }

    public Optional<PowderSpecialTierInfo> getPowderSpecial() {
        return powderSpecial;
    }

    public void setPowders(List<Pair<Powder, Integer>> powders) {
        this.powders = List.copyOf(powders);
    }

    public void setPowderSpecial(Optional<PowderSpecialTierInfo> powderSpecial) {
        this.powderSpecial = powderSpecial;
    }

    public void updateStats(GearInfo gearInfo, List<StatActualValue> identifications, int rerolls) {
        this.identifications = List.copyOf(identifications);
        this.rerolls = rerolls;
        this.overallQuality = StatCalculator.calculateOverallQuality(
                gearInfo.name(), gearInfo.getPossibleValueList(), identifications);
    }

    public static GearInstance create(GearInfo gearInfo, List<StatActualValue> identifications, int rerolls) {
        return new GearInstance(
                identifications,
                List.of(),
                rerolls,
                StatCalculator.calculateOverallQuality(
                        gearInfo.name(), gearInfo.getPossibleValueList(), identifications));
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

    public StatActualValue getActualValue(StatType statType) {
        return identifications.stream()
                .filter(s -> s.statType().equals(statType))
                .findFirst()
                .orElse(null);
    }
}
