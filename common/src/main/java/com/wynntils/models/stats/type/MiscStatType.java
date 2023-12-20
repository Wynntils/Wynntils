/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.models.stats.builders.MiscStatKind;
import com.wynntils.utils.type.RangedValue;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public final class MiscStatType extends StatType {
    private final MiscStatKind kind;

    public MiscStatType(
            String key,
            String displayName,
            String apiName,
            String internalRollName,
            StatUnit unit,
            SpecialStatType specialStatType,
            MiscStatKind kind) {
        super(key, displayName, apiName, internalRollName, unit, specialStatType);
        this.kind = kind;
    }

    public MiscStatKind getKind() {
        return kind;
    }

    @Override
    public StatCalculationInfo getStatCalculationInfo(int baseValue) {
        // Charm stats have a custom range
        if (getSpecialStatType() == SpecialStatType.CHARM_LEVELED_STAT) {
            return new StatCalculationInfo(
                    RangedValue.of(80, 115),
                    calculateAsInverted() ? RoundingMode.HALF_DOWN : RoundingMode.HALF_UP,
                    Optional.of(1),
                    Optional.empty(),
                    List.of());
        }

        return super.getStatCalculationInfo(baseValue);
    }
}
