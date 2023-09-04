/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatValue;
import java.util.List;
import java.util.Optional;

public class ActualStatProvider extends ItemStatProvider<StatValue> {
    private final StatType statType;

    public ActualStatProvider(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String getName() {
        return statType.getApiName();
    }

    @Override
    public String getDescription() {
        return getTranslation("description", statType.getDisplayName());
    }

    @Override
    public List<StatValue> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearItem gearItem)) return List.of();

        GearInfo gearInfo = gearItem.getGearInfo();
        StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);

        if (possibleValues == null) {
            return List.of();
        }

        Optional<GearInstance> gearInstanceOpt = gearItem.getGearInstance();

        if (gearInstanceOpt.isEmpty()) {
            // Item guide item
            return List.of(new StatValue(-1, possibleValues, null));
        }

        StatActualValue actualValue = gearInstanceOpt.get().getActualValue(statType);

        // The item is revealed, no actual stats yet
        if (actualValue == null) {
            return List.of(new StatValue(-1, possibleValues, null));
        }

        float percentage = StatCalculator.getPercentage(actualValue, possibleValues);
        return List.of(new StatValue(percentage, possibleValues, actualValue));
    }
}
