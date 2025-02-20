/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.usermarker.type;

import com.wynntils.core.components.Services;
import com.wynntils.services.mapdata.attributes.MapMarkerOptionsBuilder;
import com.wynntils.services.mapdata.attributes.impl.MapLocationAttributesImpl;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.services.mapdata.providers.builtin.MapIconsProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.Texture;
import java.util.Optional;

public final class UserMarkerLocation extends MapLocationImpl {
    private final String name;
    private final MapLocationAttributesImpl userMarkerAttributes;

    public UserMarkerLocation(Location location, String name) {
        super("user-marker-" + location.hashCode(), "wynntils:personal:user-marker", null, location);
        this.name = name;
        this.userMarkerAttributes = Services.UserMarker.MARKED_MAP_FEATURE_ATTRIBUTES
                .setIcon(MapIconsProvider.getIconIdFromTexture(Texture.WAYPOINT))
                .setLabel(name)
                .setLabelColor(CommonColors.WHITE)
                .setMarkerOptions(
                        new MapMarkerOptionsBuilder().withHasLabel(true).build())
                .asLocationAttributes()
                .build();
    }

    @Override
    public Optional<MapLocationAttributes> getAttributes() {
        return Optional.of(userMarkerAttributes);
    }
}
