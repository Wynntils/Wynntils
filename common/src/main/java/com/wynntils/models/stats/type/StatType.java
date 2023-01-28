/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import java.util.Objects;

// The key is strictly not necessary, but is internally useful
// The "loreName" is what is used in the json lore of other player's items
public abstract class StatType {
    private final String key;
    private final String displayName;
    private final String apiName;
    private final String loreName;
    private final StatUnit unit;

    public StatType(String key, String displayName, String apiName, String loreName, StatUnit unit) {
        this.key = key;
        this.displayName = displayName;
        this.apiName = apiName;
        this.loreName = loreName;
        this.unit = unit;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }

    public String getLoreName() {
        return loreName;
    }

    public StatUnit getUnit() {
        return unit;
    }

    public boolean isInverted() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StatType) obj;
        return Objects.equals(this.key, that.key)
                && Objects.equals(this.displayName, that.displayName)
                && Objects.equals(this.apiName, that.apiName)
                && Objects.equals(this.loreName, that.loreName)
                && Objects.equals(this.unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, displayName, apiName, loreName, unit);
    }

    @Override
    public String toString() {
        return "StatType[" + "key="
                + key + ", " + "displayName="
                + displayName + ", " + "apiName="
                + apiName + ", " + "loreName="
                + loreName + ", " + "unit="
                + unit + ']';
    }
}
