/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import java.util.Optional;

public interface MapAreaAttributes extends MapAttributes {
    static List<String> getUnsupportedAttributes() {
        return List.of("iconId", "iconVisibility", "iconColor", "iconDecoration");
    }

    default Optional<String> getIconId() {
        return Optional.empty();
    }

    default Optional<MapVisibility> getIconVisibility() {
        return Optional.empty();
    }

    default Optional<CustomColor> getIconColor() {
        return Optional.empty();
    }

    default Optional<MapDecoration> getIconDecoration() {
        return Optional.empty();
    }
}
