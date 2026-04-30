package com.wynntils.models.characterstats.type;

import com.wynntils.models.stats.type.StatType;

public record PlayerStat(StatType statType, int value, boolean isPositive) {
    public static final PlayerStat NONE = new PlayerStat(null, 0, false);

    public String toDisplay() {
        if(statType == null) return "";
        return (isPositive ? "+" : "") + value + statType.getUnit().getDisplayName();
    }
}