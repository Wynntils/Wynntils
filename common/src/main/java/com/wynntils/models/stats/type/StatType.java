/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import java.util.Objects;

// The key is strictly not necessary, but is internally useful
// The "internalRollName" is what is used in the json lore of other player's items
public abstract class StatType {
    private final String key;
    private final String displayName;
    private final String apiName;
    private final String internalRollName;
    private final StatUnit unit;

    protected StatType(String key, String displayName, String apiName, String internalRollName, StatUnit unit) {
        this.key = key;
        this.displayName = displayName;
        this.apiName = apiName;
        this.internalRollName = internalRollName;
        this.unit = unit;
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

    public boolean showAsInverted() {
        return false;
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
}
