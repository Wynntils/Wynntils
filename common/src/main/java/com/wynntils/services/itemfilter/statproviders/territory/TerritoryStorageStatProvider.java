/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.territory;

import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class TerritoryStorageStatProvider extends TerritoryStatProvider<CappedValue> {
    private final GuildResource guildResource;

    public TerritoryStorageStatProvider(GuildResource guildResource) {
        this.guildResource = guildResource;
    }

    @Override
    public Optional<CappedValue> getValue(TerritoryItem territoryItem) {
        return Optional.ofNullable(territoryItem.getStorage().get(guildResource));
    }

    @Override
    public String getName() {
        return guildResource.getName().toLowerCase(Locale.ROOT) + "Storage";
    }

    @Override
    public String getDisplayName() {
        return guildResource.getName() + " Storage";
    }

    @Override
    public List<String> getAliases() {
        return List.of(guildResource.getName().toLowerCase(Locale.ROOT) + "Str");
    }

    @Override
    public String getDescription() {
        return getTranslation("description", guildResource.getName());
    }
}
