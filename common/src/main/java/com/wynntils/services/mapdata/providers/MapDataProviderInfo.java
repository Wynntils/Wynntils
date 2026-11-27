/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers;

import com.wynntils.services.mapdata.providers.type.MapDataProvider;

/**
 * Registry entry for a map data provider, tracking who registered it and whether users may disable it.
 *
 * @param provider         The provider itself
 * @param owner            The component that registered the provider, or a descriptive string (e.g. "user" for JSON providers)
 * @param toggleable       Whether users can enable/disable the provider. Structural providers (categories, icons) must not
 *                         be toggleable, as other providers' features depend on them for attribute and icon resolution.
 * @param enabledByDefault Whether the provider is enabled when the user has not overridden it
 */
public record MapDataProviderInfo(
        MapDataProvider provider, Object owner, boolean toggleable, boolean enabledByDefault) {
    public MapDataProviderInfo withProvider(MapDataProvider newProvider) {
        return new MapDataProviderInfo(newProvider, owner, toggleable, enabledByDefault);
    }

    public String getOwnerName() {
        return owner instanceof String ownerName ? ownerName : owner.getClass().getSimpleName();
    }
}
