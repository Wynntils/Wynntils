/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.core.components.Services;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;

public class FavoriteStatProvider extends ItemStatProvider<Boolean> {
    @Override
    public Optional<Boolean> getValue(WynnItem wynnItem) {
        if (wynnItem instanceof NamedItemProperty namedItem) {
            return Optional.of(Services.Favorites.isFavorite(namedItem.getName()));
        }

        return Optional.of(false);
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GENERIC);
    }

    @Override
    public List<String> getAliases() {
        return List.of("fav");
    }
}
