/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.EnumUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AttackSpeedStatProvider extends ItemStatProvider<String> {
    @Override
    public Optional<String> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearItem gearItem)) return Optional.empty();
        if (gearItem.getItemInfo().fixedStats().attackSpeed().isEmpty()) return Optional.empty();

        return Optional.of(gearItem.getItemInfo()
                .fixedStats()
                .attackSpeed()
                .get()
                .getName()
                .replace(" ", "_"));
    }

    @Override
    public List<String> getValidInputs() {
        // Requires the underscore to be valid
        return Arrays.stream(GearAttackSpeed.values())
                .map(gearAttackSpeed -> EnumUtils.toNiceString(gearAttackSpeed).replace(" ", "_"))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR);
    }
}
