/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.services.mapdata.features.builtin.TerritoryArea;
import com.wynntils.services.mapdata.features.type.MapArea;
import com.wynntils.services.usermarker.type.UserMarkerLocation;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2fc;

@ConfigCategory(Category.MAP)
public class TerritoryBordersFeature extends Feature {
    @Persisted
    private final Config<Double> renderDistance = new Config<>(500d);

    @Persisted
    private final Config<Boolean> onlyRenderCompassedTerritories = new Config<>(false);

    @Persisted
    private final Config<BorderStyle> style = new Config<>(BorderStyle.LAYERS);

    @Persisted
    private final Config<Integer> borderThickness = new Config<>(2);

    @Persisted
    private final Config<CustomColor> borderColor = new Config<>(CommonColors.RED);

    public TerritoryBordersFeature() {
        super(ProfileDefault.DISABLED, List.of(ConfigDependency.functionality(Models.Territory.lookupApiInfo)));
    }

    @SubscribeEvent
    public void onPostRenderLevelEvent(RenderLevelEvent.Post event) {
        Vec3 cameraPosition = event.getCamera().position();
        GizmoStyle borderStyle = GizmoStyle.stroke(borderColor.get().asInt(), borderThickness.get());
        List<TerritoryProfile> territoryProfiles = Services.MapData.getFeaturesForCategory("wynntils:territory")
                .filter(f -> f instanceof TerritoryArea)
                .map(f -> (TerritoryArea) f)
                .filter(territory -> filterRenderDistance(territory, cameraPosition))
                .filter(this::filterOnlyRenderCompassedTerritories)
                .map(TerritoryArea::getTerritoryProfile)
                .toList();

        for (TerritoryProfile territoryProfile : territoryProfiles)
            switch (style.get()) {
                case BOX:
                    Gizmos.cuboid(
                            new AABB(
                                    territoryProfile.getStartX(),
                                    0,
                                    territoryProfile.getStartZ(),
                                    territoryProfile.getEndX(),
                                    256,
                                    territoryProfile.getEndZ()),
                            borderStyle);
                    break;
                case LAYERS:
                    for (int level = (int) (cameraPosition.y() / 4 - 20); level < cameraPosition.y() / 4 + 20; level++)
                        Gizmos.cuboid(
                                new AABB(
                                        territoryProfile.getStartX(),
                                        4 * level,
                                        territoryProfile.getStartZ(),
                                        territoryProfile.getEndX(),
                                        4 * level,
                                        territoryProfile.getEndZ()),
                                borderStyle);
                    break;
            }
    }

    private boolean filterRenderDistance(MapArea territoryArea, Vec3 cameraPosition) {
        Vector2fc territoryCentroid = territoryArea.getBoundingPolygon().centroid();
        Vector2dc flatPosition = new Vector2d(cameraPosition.x, cameraPosition.z);

        return flatPosition.distance(territoryCentroid) < renderDistance.get();
    }

    private boolean filterOnlyRenderCompassedTerritories(MapArea territoryArea) {
        if (!onlyRenderCompassedTerritories.get()) return true;

        return !Services.UserMarker.getMarkedFeatures()
                .filter(f -> f instanceof UserMarkerLocation)
                .map(f -> (UserMarkerLocation) f)
                .filter(compassMarkerLocation -> {
                    Location compassLocation = compassMarkerLocation.getLocation();
                    return territoryArea.getBoundingPolygon().contains(compassLocation.x, compassLocation.z);
                })
                .toList()
                .isEmpty();
    }

    private enum BorderStyle {
        BOX,
        LAYERS;
    }
}
