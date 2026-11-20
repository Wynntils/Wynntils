/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.type.MapArea;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.PrismStyle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2fc;

@ConfigCategory(Category.MAP)
public class RenderMapAreasFeature extends Feature {
    @Persisted
    private final Config<Double> renderDistance = new Config<>(500d);

    public RenderMapAreasFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent
    public void onPostRenderLevelEvent(RenderLevelEvent.Post event) {
        Vec3 cameraPosition = event.getCamera().position();

        List<MapArea> mapAreas = Services.MapData.getFeatures()
                .filter(f -> f instanceof MapArea)
                .map(f -> (MapArea) f)
                .filter(mapArea -> filterRenderDistance(mapArea, cameraPosition))
                .toList();

        for (MapArea mapArea : mapAreas) {
            ResolvedMapAttributes mapAreaAttributes = Services.MapData.resolveMapAttributes(mapArea);

            if (!mapAreaAttributes.borderRenderInWorld()) continue;

            CustomColor color = mapAreaAttributes.borderWorldColor();
            float width = mapAreaAttributes.borderWorldWidth();
            PrismStyle style = mapAreaAttributes.borderWorldStyle();

            switch (style) {
                case SIMPLE: {
                    List<Location> bottomVertices = mapArea.getPolygonArea();
                    List<Location> topVertices = new ArrayList<>();
                    for (Location bottomVertex : bottomVertices)
                        topVertices.add(new Location(bottomVertex.x, 256, bottomVertex.z));

                    RenderUtils.drawPolygon(bottomVertices, color.asInt(), width);
                    RenderUtils.drawPolygon(topVertices, color.asInt(), width);
                    for (int i = 0; i < bottomVertices.size(); ++i) {
                        Location bottomVertex = bottomVertices.get(i);
                        Location topVertex = topVertices.get(i);
                        Gizmos.line(bottomVertex.toVec3(), topVertex.toVec3(), color.asInt(), width);
                    }

                    break;
                }

                case LAYERS: {
                    for (int level = (int) (cameraPosition.y() / 4 - 20);
                            level < cameraPosition.y() / 4 + 20;
                            level++) {
                        List<Location> vertices = new ArrayList<>();
                        for (Location referenceVertex : mapArea.getPolygonArea())
                            vertices.add(new Location(referenceVertex.x, 4 * level, referenceVertex.z));

                        RenderUtils.drawPolygon(vertices, color.asInt(), width);
                    }

                    break;
                }
            }
        }
    }

    private boolean filterRenderDistance(MapArea mapArea, Vec3 cameraPosition) {
        Vector2fc territoryCentroid = mapArea.getBoundingPolygon().centroid();
        Vector2dc flatPosition = new Vector2d(cameraPosition.x, cameraPosition.z);

        return flatPosition.distance(territoryCentroid) < renderDistance.get();
    }

    private enum BorderStyle {
        BOX,
        LAYERS;
    }
}
