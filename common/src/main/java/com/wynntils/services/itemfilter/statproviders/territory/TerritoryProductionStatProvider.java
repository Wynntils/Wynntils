/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.territory;

import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.type.GuildResource;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class TerritoryProductionStatProvider extends TerritoryStatProvider<Integer> {
    private final GuildResource guildResource;

    public TerritoryProductionStatProvider(GuildResource guildResource) {
        this.guildResource = guildResource;
    }

    @Override
    public Optional<Integer> getValue(TerritoryItem territoryItem) {
        Integer generation = territoryItem.getProduction().get(guildResource);
        return generation == null ? Optional.empty() : Optional.of(generation);
    }

    @Override
    public String getName() {
        return guildResource.getName().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getDisplayName() {
        return guildResource.getName() + " Production";
    }

    @Override
    public List<String> getAliases() {
        return List.of(
                guildResource.getName().toLowerCase(Locale.ROOT) + "Production",
                guildResource.getName().toLowerCase(Locale.ROOT) + "Prod");
    }

    @Override
    public String getDescription() {
        return getTranslation("description", guildResource.getName());
    }
}
