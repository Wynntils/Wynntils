/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.services.map.type.ServiceKind;
import com.wynntils.services.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.impl.FadingMapVisiblity;
import com.wynntils.services.mapdata.attributes.impl.NeverMapVisibility;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.services.mapdata.type.MapLocation;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ServiceListProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();
    private static int counter;

    @Override
    public String getProviderId() {
        return "service-list";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    public static void registerFeature(Location location, ServiceKind kind) {
        PROVIDED_FEATURES.add(new ServiceLocation(location, kind));
    }

    private static final class ServiceLocation implements MapLocation {
        private static final FadingMapVisiblity FAST_TRAVEL_VISIBLITY = new FadingMapVisiblity(18, 100, 6);
        private static final FadingMapVisiblity OTHER_VISIBLITY = new FadingMapVisiblity(57, 100, 6);

        private final Location location;
        private final ServiceKind kind;
        private final int number;

        private ServiceLocation(Location location, ServiceKind kind) {
            this.location = location;
            this.kind = kind;
            this.number = ServiceListProvider.counter++;
        }

        @Override
        public String getFeatureId() {
            return kind.getMapDataId() + "-" + number;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:service:" + kind.getMapDataId();
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public MapVisibility getIconVisibility() {
                    if (kind == ServiceKind.FAST_TRAVEL) {
                        return FAST_TRAVEL_VISIBLITY;
                    } else {
                        return OTHER_VISIBLITY;
                    }
                }

                @Override
                public MapVisibility getLabelVisibility() {
                    return new NeverMapVisibility();
                }
            };
        }

        @Override
        public List<String> getTags() {
            return List.of();
        }

        @Override
        public Location getLocation() {
            // FIXME: debug
            return location.offset(15, 0, 15);
        }
    }
}
