/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.territory;

import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.models.territories.type.TerritoryStorage;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Locale;

public class TerritoryStorageStatProvider extends TerritoryStatProvider<CappedValue> {
    private final GuildResource guildResource;

    public TerritoryStorageStatProvider(GuildResource guildResource) {
        this.guildResource = guildResource;
    }

    @Override
    public List<CappedValue> getValue(TerritoryItem territoryItem) {
        TerritoryStorage territoryStorage = territoryItem.getStorage().get(guildResource);
        return territoryStorage == null
                ? List.of()
                : List.of(new CappedValue(territoryStorage.current(), territoryStorage.max()));
    }

    @Override
    public String getName() {
        return guildResource.getName().toLowerCase(Locale.ROOT) + "Storage";
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
