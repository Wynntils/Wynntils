/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.seaskipper.type;

import com.wynntils.core.components.Models;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapAreaAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.services.mapdata.features.type.MapArea;
import com.wynntils.utils.MapDataUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.BoundingPolygon;
import java.util.List;
import java.util.Optional;

public class SeaskipperDestinationArea implements MapArea {
    private final SeaskipperDestination destination;
    private final List<Location> polygonArea;
    private final BoundingPolygon boundingPolygon;

    public SeaskipperDestinationArea(SeaskipperDestination destination) {
        this.destination = destination;

        this.polygonArea = destination.profile().points();
        this.boundingPolygon = BoundingPolygon.fromLocations(polygonArea);
    }

    @Override
    public List<Location> getPolygonArea() {
        return polygonArea;
    }

    @Override
    public BoundingPolygon getBoundingPolygon() {
        return boundingPolygon;
    }

    @Override
    public String getFeatureId() {
        return MapDataUtils.sanitizeFeatureId(destination.profile().destination());
    }

    @Override
    public String getCategoryId() {
        return "wynntils:fast-travel:seaskipper-destination";
    }

    @Override
    public Optional<MapAreaAttributes> getAttributes() {
        return Optional.of(new AbstractMapAreaAttributes() {
            @Override
            public Optional<String> getLabel() {
                return Optional.of(destination.profile().destination());
            }

            @Override
            public Optional<CustomColor> getLabelColor() {
                return Optional.of(getColor());
            }

            @Override
            public Optional<CustomColor> getFillColor() {
                return Optional.of(getColor().withAlpha(80));
            }

            @Override
            public Optional<CustomColor> getBorderColor() {
                return Optional.of(getColor());
            }

            private CustomColor getColor() {
                if (destination.isPlayerInside()) return CommonColors.ORANGE;
                return destination.isAvailable()
                                && Models.Emerald.getAmountInInventory()
                                        >= destination.item().getPrice()
                        ? CommonColors.GREEN
                        : CommonColors.RED;
            }
        });
    }

    @Override
    public List<String> getTags() {
        return List.of();
    }

    public SeaskipperDestination getDestination() {
        return destination;
    }
}
