/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.utils.type.RangedValue;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// The key is strictly not necessary, but is internally useful
// The "internalRollName" is what is used in the json lore of other player's items
public abstract class StatType {
    // These ranges are used everywhere, except charms
    private static final List<RangedValue> STAR_INTERNAL_ROLL_RANGES = List.of(
            RangedValue.of(30, 100), // 0 stars
            RangedValue.of(101, 124), // 1 star
            RangedValue.of(125, 129), // 2 stars
            RangedValue.of(130, 130) // 3 stars
            );

    private final String key;
    private final String displayName;
    private final String apiName;
    private final String internalRollName;
    private final StatUnit unit;
    private final SpecialStatType specialStatType;

    protected StatType(String key, String displayName, String apiName, String internalRollName, StatUnit unit) {
        this.key = key;
        this.displayName = displayName;
        this.apiName = apiName;
        this.internalRollName = internalRollName;
        this.unit = unit;
        this.specialStatType = SpecialStatType.NONE;
    }

    protected StatType(
            String key,
            String displayName,
            String apiName,
            String internalRollName,
            StatUnit unit,
            SpecialStatType specialStatType) {
        this.key = key;
        this.displayName = displayName;
        this.apiName = apiName;
        this.internalRollName = internalRollName;
        this.unit = unit;
        this.specialStatType = specialStatType;
    }

    public String getKey() {
        return key;
    }

    // Most likely, you'll want to use Models.Stat.getDisplayName instead, since it will make
    // spell cost stats display correctly.
    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }

    public String getInternalRollName() {
        return internalRollName;
    }

    public StatUnit getUnit() {
        return unit;
    }

    public StatCalculationInfo getStatCalculationInfo(int baseValue) {
        boolean usePositiveRange = baseValue > 0;
        return usePositiveRange
                ? new StatCalculationInfo(
                        RangedValue.of(30, 130),
                        calculateAsInverted() ? RoundingMode.HALF_DOWN : RoundingMode.HALF_UP,
                        Optional.of(1),
                        Optional.empty(),
                        treatAsInverted() ? List.of() : STAR_INTERNAL_ROLL_RANGES)
                : new StatCalculationInfo(
                        RangedValue.of(70, 130),
                        calculateAsInverted() ? RoundingMode.HALF_UP : RoundingMode.HALF_DOWN,
                        Optional.empty(),
                        Optional.of(-1),
                        List.of());
    }

    /**
     * Whether the stat should be displayed as inverted.
     * This should be true if a value with a positive sign should be displayed as negative.
     * Usually this should be used in combination with {@link #calculateAsInverted()} or {@link #treatAsInverted()}.
     * @return true if the stat should be displayed as inverted, false otherwise
     */
    public boolean displayAsInverted() {
        return false;
    }

    /**
     * Whether the stat should be treated as negative when calculating the total stat value.
     * This is used when calculating the percentage values of a stat.
     * <p><b>
     *     Note that this does not modify the calculated internal roll, deliberately.
     *     This means that the highest internal roll value will result in the "worst" stat value.
     * </b></p>
     *
     * <p> Use this if a stat has an inverted effect, compared to a base stat, but needs to be treated according to the base stat's sign. </p>
     * @return true if the stat should be treated as negative, false otherwise
     */
    public boolean treatAsInverted() {
        return false;
    }

    /**
     * Whether the stat should be calculated as inverted (the base value is given a negative sign before being used in calculations).
     * @return true if the stat should be calculated as inverted, false otherwise
     */
    public boolean calculateAsInverted() {
        return false;
    }

    public SpecialStatType getSpecialStatType() {
        return specialStatType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        StatType that = (StatType) obj;
        return Objects.equals(this.key, that.key)
                && Objects.equals(this.displayName, that.displayName)
                && Objects.equals(this.apiName, that.apiName)
                && Objects.equals(this.internalRollName, that.internalRollName)
                && this.unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, displayName, apiName, internalRollName, unit);
    }

    @Override
    public String toString() {
        return "StatType[" + "key="
                + key + ", " + "displayName="
                + displayName + ", " + "apiName="
                + apiName + ", " + "internalRollName="
                + internalRollName + ", " + "unit="
                + unit + ']';
    }

    public enum SpecialStatType {
        NONE,

        // Tomes have a special stats that are not variable,
        // and are not displayed as regular stats
        TOME_BASE_STAT,

        // Some stats on charms only apply to a level range of mobs/players
        CHARM_LEVELED_STAT
    }
}
