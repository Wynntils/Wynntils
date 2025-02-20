/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.usermarker.type;

import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.impl.MapLocationAttributesImpl;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.utils.mc.type.Location;
import java.util.Optional;

public final class UserMarkerLocation extends MapLocationImpl {
    private final String name;
    private final MapLocationAttributesImpl userMarkerAttributes;

    public UserMarkerLocation(Location location, String name) {
        super("user-marker-" + location.hashCode(), "wynntils:personal:user-marker", null, location);
        this.name = name;
        this.userMarkerAttributes =
                new MapAttributesBuilder().setLabel(name).asLocationAttributes().build();
    }

    @Override
    public Optional<MapLocationAttributes> getAttributes() {
        return Optional.of(userMarkerAttributes);
    }
}
