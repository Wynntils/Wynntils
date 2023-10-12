/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.ProfessionItemProperty;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class ProfessionStatProvider extends ItemStatProvider<String> {
    @Override
    public List<String> getValue(ItemStack itemStack, WynnItem wynnItem) {
        if (!(wynnItem instanceof ProfessionItemProperty professionItemProperty)) return List.of();

        return professionItemProperty.getProfessionTypes().stream()
                .map(ProfessionType::getDisplayName)
                .toList();
    }

    @Override
    public List<String> getAliases() {
        return List.of("prof");
    }
}
