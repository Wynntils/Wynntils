/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

public final class DefenceStatType extends StatType {
    private final boolean invertedStat;

    public DefenceStatType(String key, String displayName, String apiName, String internalRollName, StatUnit unit) {
        super(key, displayName, apiName, internalRollName, unit);
        this.invertedStat = false;
    }

    public DefenceStatType(
            String key,
            String displayName,
            String apiName,
            String internalRollName,
            StatUnit unit,
            SpecialStatType specialStatType,
            boolean invertedStat) {
        super(key, displayName, apiName, internalRollName, unit, specialStatType);
        this.invertedStat = invertedStat;
    }

    @Override
    public boolean showAsInverted() {
        return invertedStat;
    }
}
