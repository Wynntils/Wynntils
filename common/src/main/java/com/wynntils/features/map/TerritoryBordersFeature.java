/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.services.map.pois.TerritoryPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

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
        Set<TerritoryProfile> territories = Models.Territory.getTerritoryPois().stream()
                .map(TerritoryPoi::getTerritoryProfile)
                .filter(territory -> cameraPosition.distanceTo(
                                territory.getCenterLocation().asLocation().toVec3())
                        < renderDistance.get())
                .filter(territory -> !onlyRenderCompassedTerritories.get()
                        || !Models.Marker.USER_WAYPOINTS_PROVIDER
                                .getMarkerInfos()
                                .filter(marker ->
                                        territory.insideArea(marker.location().toPosition()))
                                .collect(Collectors.toSet())
                                .isEmpty())
                .collect(Collectors.toSet());

        for (TerritoryProfile territory : territories)
            switch (style.get()) {
                case BOX:
                    Gizmos.cuboid(
                            new AABB(
                                    territory.getStartX(),
                                    0,
                                    territory.getStartZ(),
                                    territory.getEndX(),
                                    256,
                                    territory.getEndZ()),
                            borderStyle);
                    break;
                case LAYERS:
                    for (int level = Integer.max(0, (int) (cameraPosition.y() / 4 - 20));
                            level < cameraPosition.y() / 4 + 20 && 4 * level < 256;
                            level++)
                        Gizmos.cuboid(
                                new AABB(
                                        territory.getStartX(),
                                        4 * level,
                                        territory.getStartZ(),
                                        territory.getEndX(),
                                        4 * level,
                                        territory.getEndZ()),
                                borderStyle);
                    break;
            }
    }

    private enum BorderStyle {
        BOX,
        LAYERS;
    }
}
