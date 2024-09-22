/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class RarityStatProvider extends ItemStatProvider<String> {
    @Override
    public Optional<String> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearTierItemProperty gearTierItemProperty)) return Optional.empty();

        return Optional.of(gearTierItemProperty.getGearTier().getName());
    }

    @Override
    public List<String> getValidInputs() {
        return Arrays.stream(GearTier.values()).map(GearTier::getName).collect(Collectors.toList());
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR);
    }

    @Override
    public int compare(WynnItem wynnItem1, WynnItem wynnItem2) {
        Optional<String> itemValue1 = this.getValue(wynnItem1);
        Optional<String> itemValue2 = this.getValue(wynnItem2);

        if (itemValue1.isEmpty() && itemValue2.isPresent()) return 1;
        if (itemValue1.isPresent() && itemValue2.isEmpty()) return -1;
        if (itemValue1.isEmpty() && itemValue2.isEmpty()) return 0;

        // Map the string values to the GearTier enum values
        GearTier gearTier1 = GearTier.valueOf(itemValue1.get().toUpperCase(Locale.ROOT));
        GearTier gearTier2 = GearTier.valueOf(itemValue2.get().toUpperCase(Locale.ROOT));

        return -gearTier1.compareTo(gearTier2);
    }
}
