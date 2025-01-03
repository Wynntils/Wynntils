/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import java.util.List;

public interface MapAreaAttributes extends MapAttributes {
    static List<String> getUnsupportedAttributes() {
        return List.of("iconId", "iconVisibility", "iconColor", "iconDecoration", "hasMarker", "markerOptions");
    }
}
