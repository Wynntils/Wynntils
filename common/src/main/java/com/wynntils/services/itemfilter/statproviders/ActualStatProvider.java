/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
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
        if (wynnItem instanceof GearItem gearItem) {
            return handleGearItem(gearItem);
        }

        if (wynnItem instanceof IngredientItem ingredientItem) {
            return handleIngredientItem(ingredientItem);
        }

        return List.of();
    }

    private List<StatValue> handleIngredientItem(IngredientItem ingredientItem) {
        IngredientInfo ingredientInfo = ingredientItem.getIngredientInfo();

        if (ingredientInfo == null) {
            return List.of();
        }

        List<Pair<StatType, RangedValue>> stats = ingredientInfo.variableStats();

        return stats.stream()
                .filter(pair -> pair.key().equals(statType))
                .map(pair -> new StatValue(new StatPossibleValues(pair.key(), pair.value(), 0, false), null))
                .toList();
    }

    private List<StatValue> handleGearItem(GearItem gearItem) {
        GearInfo gearInfo = gearItem.getItemInfo();
        StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);

        if (possibleValues == null) {
            return List.of();
        }

        Optional<GearInstance> gearInstanceOpt = gearItem.getItemInstance();

        if (gearInstanceOpt.isEmpty()) {
            // Item guide item
            return List.of(new StatValue(possibleValues, null));
        }

        StatActualValue actualValue = gearInstanceOpt.get().getActualValue(statType);

        // The item is revealed, no actual stats yet
        if (actualValue == null) {
            return List.of(new StatValue(possibleValues, null));
        }

        return List.of(new StatValue(possibleValues, actualValue));
    }

    @Override
    public int compare(WynnItem wynnItem1, WynnItem wynnItem2) {
        List<StatValue> itemValues1 = this.getValue(wynnItem1);
        List<StatValue> itemValues2 = this.getValue(wynnItem2);

        if (itemValues1.isEmpty() && !itemValues2.isEmpty()) return 1;
        if (!itemValues1.isEmpty() && itemValues2.isEmpty()) return -1;
        if (itemValues1.isEmpty() && itemValues2.isEmpty()) return 0;

        return -itemValues1.get(0).compareTo(itemValues2.get(0));
    }
}
