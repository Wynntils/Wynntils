/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.TomeType;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.EnumUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TomeTypeStatProvider extends ItemStatProvider<String> {
    @Override
    public Optional<String> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof TomeItem tomeItem)) return Optional.empty();

        return Optional.of(tomeItem.getItemInfo().type().name());
    }

    @Override
    public List<String> getValidInputs() {
        return Arrays.stream(TomeType.values())
                .map(tomeType -> EnumUtils.toNiceString(tomeType).replace(" ", "_"))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR);
    }
}
