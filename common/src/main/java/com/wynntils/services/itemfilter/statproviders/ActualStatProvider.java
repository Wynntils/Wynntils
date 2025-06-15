/*
 * Copyright © Wynntils 2023-2025.
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
import com.wynntils.services.itemfilter.type.ItemProviderType;
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
    public String getDisplayName() {
        // A few overrides for clearer names
        // and to show units for non raw stats
        if (statType.getKey().equals("MISC_LEVELED_XP_BONUS")) {
            return "XP From Lv. Content";
        } else if (statType.getKey().equals("MISC_LEVELED_LOOT_BONUS")) {
            return "Loot From Lv. Content";
        } else {
            return switch (statType.getUnit()) {
                case PERCENT, PER_3_S, PER_5_S ->
                    statType.getDisplayName() + "(" + statType.getUnit().getDisplayName() + ")";
                default -> statType.getDisplayName();
            };
        }
    }

    @Override
    public String getDescription() {
        return getTranslation("description", statType.getDisplayName());
    }

    @Override
    public Optional<StatValue> getValue(WynnItem wynnItem) {
        if (wynnItem instanceof GearItem gearItem) {
            return handleGearItem(gearItem);
        }

        if (wynnItem instanceof IngredientItem ingredientItem) {
            return handleIngredientItem(ingredientItem);
        }

        return Optional.empty();
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR, ItemProviderType.GEAR_INSTANCE, ItemProviderType.INGREDIENT);
    }

    private Optional<StatValue> handleIngredientItem(IngredientItem ingredientItem) {
        IngredientInfo ingredientInfo = ingredientItem.getIngredientInfo();

        if (ingredientInfo == null) {
            return Optional.empty();
        }

        List<Pair<StatType, RangedValue>> stats = ingredientInfo.variableStats();

        return stats.stream()
                .filter(pair -> pair.key().equals(statType))
                .map(pair -> new StatValue(new StatPossibleValues(pair.key(), pair.value(), 0, false), null))
                .findFirst();
    }

    private Optional<StatValue> handleGearItem(GearItem gearItem) {
        GearInfo gearInfo = gearItem.getItemInfo();
        StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);

        if (possibleValues == null) {
            return Optional.empty();
        }

        Optional<GearInstance> gearInstanceOpt = gearItem.getItemInstance();

        if (gearInstanceOpt.isEmpty()) {
            // Item guide item
            return Optional.of(new StatValue(possibleValues, null));
        }

        StatActualValue actualValue = gearInstanceOpt.get().getActualValue(statType);

        // The item is revealed, no actual stats yet
        if (actualValue == null) {
            return Optional.of(new StatValue(possibleValues, null));
        }

        return Optional.of(new StatValue(possibleValues, actualValue));
    }

    @Override
    public List<String> getAliases() {
        // Add "defense" as an alias for all defence stat types
        if (statType.getApiName().contains("defence")) {
            return List.of(statType.getApiName().replace("defence", "defense"));
        } else if (statType.getApiName().contains("Defence")) {
            return List.of(statType.getApiName().replace("Defence", "Defense"));
        }

        return super.getAliases();
    }
}
