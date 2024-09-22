/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.ProfessionItemProperty;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ProfessionStatProvider extends ItemStatProvider<Boolean> {
    private final ProfessionType professionType;

    public ProfessionStatProvider(ProfessionType professionType) {
        this.professionType = professionType;
    }

    @Override
    public String getName() {
        return professionType.getDisplayName().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getDisplayName() {
        return professionType.getDisplayName();
    }

    @Override
    public String getDescription() {
        return getTranslation("description", professionType.getDisplayName());
    }

    @Override
    public Optional<Boolean> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof ProfessionItemProperty professionItemProperty)) return Optional.empty();

        return Optional.of(professionItemProperty.getProfessionTypes().contains(professionType));
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.PROFESSION);
    }
}
