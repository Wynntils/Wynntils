/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.models.mapdata.style.MapIcon;
import com.wynntils.utils.render.Texture;
import java.util.Map;
import java.util.Optional;

public class MapIconDatabase {
    private static final Map<String, Texture> BUILT_IN = Map.of("wynntils:icon:waypoint", Texture.WAYPOINT);

    public Optional<Texture> getIconTexture(MapIcon icon) {
        Texture texture = BUILT_IN.get(icon.getId());

        if (texture != null) {
            return Optional.of(texture);
        }

        // FIXME -- look in external database
        return Optional.empty();
    }
}
