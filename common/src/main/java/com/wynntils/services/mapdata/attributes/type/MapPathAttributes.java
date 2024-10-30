/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import java.util.List;

public interface MapPathAttributes extends MapAttributes {
    static List<String> getUnsupportedAttributes() {
        return List.of(
                "iconId",
                "iconVisibility",
                "iconColor",
                "iconDecoration",
                "hasMarker",
                "markerOptions",
                "fillColor",
                "borderWidth",
                "borderColor");
    }
}
