/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.builtin;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.TerritoryInfo;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapAreaAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.services.mapdata.features.type.MapArea;
import com.wynntils.utils.MapDataUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.BoundingPolygon;
import java.util.List;
import java.util.Optional;

public class TerritoryArea implements MapArea {
    // TerritoryProfile always must be present for the territory to be displayed
    private final TerritoryProfile territoryProfile;

    // TerritoryInfo is optional and may be null, if any parsing errors occurred
    private final TerritoryInfo territoryInfo;

    private final List<Location> polygonArea;
    private final BoundingPolygon boundingPolygon;

    public TerritoryArea(TerritoryProfile territoryProfile, TerritoryInfo territoryInfo) {
        this.territoryProfile = territoryProfile;
        this.territoryInfo = territoryInfo;

        // Polygon vertices, with the last vertex connecting to the first,
        // in a counterclockwise orientation
        // The provided coordinates are not trusted to be in a specific order,
        // so we must order them ourselves
        int startX = Math.max(territoryProfile.getStartX(), territoryProfile.getEndX());
        int startZ = Math.max(territoryProfile.getStartZ(), territoryProfile.getEndZ());
        int endX = Math.min(territoryProfile.getStartX(), territoryProfile.getEndX());
        int endZ = Math.min(territoryProfile.getStartZ(), territoryProfile.getEndZ());

        this.polygonArea = List.of(
                new Location(startX, 0, startZ),
                new Location(endX, 0, startZ),
                new Location(endX, 0, endZ),
                new Location(startX, 0, endZ));
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
        return MapDataUtils.sanitizeFeatureId(territoryProfile.getName());
    }

    @Override
    public String getCategoryId() {
        return "wynntils:territory";
    }

    @Override
    public Optional<MapAreaAttributes> getAttributes() {
        return Optional.of(new AbstractMapAreaAttributes() {
            private CustomColor guildColor = Models.Guild.getColor(territoryProfile.getGuild());

            @Override
            public Optional<String> getLabel() {
                return Optional.of(territoryProfile.getGuildPrefix());
            }

            @Override
            public Optional<String> getSecondaryLabel() {
                return Optional.of(territoryProfile.getName());
            }

            @Override
            public Optional<CustomColor> getLabelColor() {
                return Optional.of(guildColor);
            }

            @Override
            public Optional<CustomColor> getFillColor() {
                return Optional.of(guildColor.withAlpha(80));
            }

            @Override
            public Optional<CustomColor> getBorderColor() {
                return Optional.of(guildColor);
            }

            @Override
            public Optional<Float> getBorderWidth() {
                return Optional.of(1f);
            }
        });
    }

    @Override
    public List<String> getTags() {
        return List.of();
    }

    public TerritoryProfile getTerritoryProfile() {
        return territoryProfile;
    }

    public Optional<TerritoryInfo> getTerritoryInfo() {
        return Optional.ofNullable(territoryInfo);
    }

    public boolean isTerritoryProfileOutdated() {
        return territoryInfo != null && !territoryInfo.getGuildName().equals(territoryProfile.getGuild());
    }
}
