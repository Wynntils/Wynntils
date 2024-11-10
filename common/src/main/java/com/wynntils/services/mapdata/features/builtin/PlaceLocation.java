/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.builtin;

import com.wynntils.services.mapdata.attributes.impl.MapLocationAttributesImpl;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.type.Location;

public final class PlaceLocation extends MapLocationImpl {
    private PlaceLocation(
            String featureId, String categoryId, MapLocationAttributesImpl attributes, Location location) {
        super(featureId, categoryId, attributes, location);
    }

    public enum PlaceType {
        PROVINCE("province"),
        CITY("city"),
        TOWN_OR_PLACE("town-or-place");

        private final String mapDataId;

        PlaceType(String mapDataId) {
            this.mapDataId = mapDataId;
        }

        public String getMapDataId() {
            return mapDataId;
        }

        public String getName() {
            return StringUtils.capitalized(mapDataId);
        }
    }
}
