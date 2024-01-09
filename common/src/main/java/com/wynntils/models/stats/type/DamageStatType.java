/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

public final class DamageStatType extends StatType {
    private final boolean invertedDisplayStat;

    public DamageStatType(String key, String displayName, String apiName, String internalRollName, StatUnit unit) {
        super(key, displayName, apiName, internalRollName, unit);
        this.invertedDisplayStat = false;
    }

    public DamageStatType(
            String key,
            String displayName,
            String apiName,
            String internalRollName,
            StatUnit unit,
            SpecialStatType specialStatType,
            boolean invertedDisplayStat) {
        super(key, displayName, apiName, internalRollName, unit, specialStatType);
        this.invertedDisplayStat = invertedDisplayStat;
    }

    @Override
    public boolean treatAsInverted() {
        return invertedDisplayStat;
    }

    @Override
    public boolean displayAsInverted() {
        return invertedDisplayStat;
    }
}
