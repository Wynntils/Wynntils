/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.mapdata;

import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.territories.type.TerritoryConnectionType;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import com.wynntils.screens.territorymanagement.TerritoryManagementHolder;
import com.wynntils.screens.territorymanagement.TerritoryManagementScreen;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapAreaAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.services.mapdata.features.type.MapArea;
import com.wynntils.utils.MapDataUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.BoundingPolygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ManageTerritoryArea implements MapArea {
    private final TerritoryManagementHolder holder;
    private final TerritoryProfile territoryProfile;

    private final List<Location> polygonArea;
    private final BoundingPolygon boundingPolygon;

    private Supplier<TerritoryItem> territoryItemSupplier;
    private TerritoryItem territoryItemCache;

    public ManageTerritoryArea(
            TerritoryManagementHolder holder,
            TerritoryProfile territoryProfile,
            Supplier<TerritoryItem> territoryItemSupplier) {
        this.holder = holder;
        this.territoryProfile = territoryProfile;
        this.territoryItemSupplier = territoryItemSupplier;

        // Polygon vertices, with the last vertex connecting to the first, in a counterclockwise
        // orientation. Mirrors TerritoryArea: the provided coordinates are not trusted to already
        // be in a specific order, so we must order them ourselves.
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

        // Prime the cache so getTerritoryItem() never returns null
        getTerritoryItem();
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

    /**
     * The category id deliberately does not start with {@code wynntils:territory}, since
     * {@link com.wynntils.services.mapdata.MapDataService#getFeaturesForCategory} does a prefix match on
     * category id - if it did start with that prefix, {@code GuildMapScreen} would pick these features up
     * alongside the real territory areas it renders.
     */
    @Override
    public String getCategoryId() {
        return "wynntils:guild:managed-territory";
    }

    @Override
    public Optional<MapAreaAttributes> getAttributes() {
        return Optional.of(new AbstractMapAreaAttributes() {
            @Override
            public Optional<String> getLabel() {
                TerritoryItem territoryItem = getTerritoryItem();

                // A checkmark or the headquarters icon is drawn over the area's center instead,
                // so the initials label would otherwise overlap it. See
                // TerritoryManagementScreen#renderTerritoryDecorations.
                if (territoryItem.isPending() || territoryItem.isSelected() || territoryItem.isHeadquarters()) {
                    return Optional.of("");
                }

                return Optional.of(getShortName(territoryItem.getName()));
            }

            @Override
            public Optional<String> getDescription() {
                // MapFeatureRenderer draws the description under the label on hover, reproducing
                // the old "full name on hover" behavior for free.
                return Optional.of(territoryProfile.getName());
            }

            @Override
            public Optional<List<CustomColor>> getFillColors() {
                return Optional.of(
                        getInfoColors().stream().map(x -> x.withAlpha(80)).toList());
            }

            @Override
            public Optional<List<CustomColor>> getBorderColors() {
                if (holder.territoryConnections().get(getTerritoryItem()) == TerritoryConnectionType.UNCONNECTED) {
                    return Optional.of(List.of(CommonColors.RED));
                }

                return Optional.of(getInfoColors());
            }

            @Override
            public Optional<CustomColor> getLabelColor() {
                return Optional.of(CustomColor.blend(getInfoColors()));
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

    public List<CustomColor> getInfoColors() {
        TerritoryItem territoryItem = getTerritoryItem();

        if (!(McUtils.screen() instanceof TerritoryManagementScreen territoryManagementScreen)) {
            return List.of(CommonColors.WHITE);
        }

        List<CustomColor> colors = new ArrayList<>();

        switch (territoryManagementScreen.getInfoType()) {
            case DEFENSE -> {
                colors.add(CustomColor.fromChatFormatting(
                        territoryItem.getDefenseDifficulty().getDefenceColor()));
                if (territoryItem.getUpgrades().getOrDefault(TerritoryUpgrade.TOWER_MULTI_ATTACKS, 0) > 0) {
                    colors.add(CustomColor.fromHSV(1 / 2f, 0.8f, 0.9f, 1));
                }
            }
            case PRODUCTION -> colors.addAll(territoryItem.getProductionColors());
            case SEEKING -> colors.addAll(territoryItem.getSeekingColors());
            case TREASURY -> colors.add(territoryItem.getTreasuryColor());
        }

        if (colors.isEmpty()) {
            colors.add(CommonColors.WHITE);
        }

        return colors;
    }

    public void onClick() {
        holder.territoryItemClicked(getTerritoryItem());
    }

    public TerritoryItem getTerritoryItem() {
        return tryGetUpdatedTerritoryItem();
    }

    void setTerritoryItemSupplier(Supplier<TerritoryItem> territoryItemSupplier) {
        this.territoryItemSupplier = territoryItemSupplier;
    }

    private TerritoryItem tryGetUpdatedTerritoryItem() {
        TerritoryItem territoryItem = territoryItemSupplier.get();
        if (territoryItem != null) {
            territoryItemCache = territoryItem;
        }
        return territoryItemCache;
    }

    private static String getShortName(String fullName) {
        return Arrays.stream(fullName.split(" ")).map(s -> s.substring(0, 1)).collect(Collectors.joining());
    }
}
