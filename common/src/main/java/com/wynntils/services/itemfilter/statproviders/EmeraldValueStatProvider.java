/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.EmeraldValuedItemProperty;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;

public class EmeraldValueStatProvider extends ItemStatProvider<Integer> {
    @Override
    public List<Integer> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof EmeraldValuedItemProperty emeraldValuedItemProperty)) return List.of();

        return List.of(emeraldValuedItemProperty.getEmeraldValue());
    }
}
