/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.mod.event.WynntilsInitEvent;
import com.wynntils.mc.event.SubmitCustomGeometryEvent;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.services.mapdata.paths.CompiledMapPath;
import com.wynntils.services.mapdata.paths.MapPathCompiler;
import com.wynntils.services.mapdata.paths.MapPathWorldRenderer;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import com.wynntils.services.mapdata.type.MapIcon;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

public final class MapPathsService extends Service {
    private static final PathCategoriesProvider PATH_CATEGORIES_PROVIDER = new PathCategoriesProvider();

    private final Map<MapPath, CachedPath> compiled = new HashMap<>();

    public MapPathsService() {
        super(List.of());
    }

    @SubscribeEvent
    public void onModInitFinished(WynntilsInitEvent.ModInitFinished event) {
        Services.MapData.registerBuiltInProvider(PATH_CATEGORIES_PROVIDER, this, false);
    }

    @SubscribeEvent
    public void renderPaths(SubmitCustomGeometryEvent event) {
        Set<MapPath> active = new HashSet<>();
        Services.MapData.getFeatures()
                .filter(MapPath.class::isInstance)
                .map(MapPath.class::cast)
                .forEach(path -> {
                    active.add(path);
                    ResolvedMapAttributes attrs = Services.MapData.resolveMapAttributes(path);
                    if (!attrs.hasPath()) return;

                    int color = attrs.pathColor().asInt();
                    CachedPath cached = compiled.get(path);
                    if (cached == null || cached.color() != color) {
                        cached = new CachedPath(color, MapPathCompiler.compile(path, attrs));
                        compiled.put(path, cached);
                    }

                    Identifier texture = attrs.pathIconId().equals(MapIcon.NO_ICON_ID)
                            ? null
                            : Services.MapData.getIconOrFallback(attrs.pathIconId())
                                    .getIdentifier();
                    MapPathWorldRenderer.renderPath(
                            event.getPoseStack(), event.getSubmitNodeCollector(), cached.compiled(), color, texture);
                });
        compiled.keySet().retainAll(active);
    }

    private record CachedPath(int color, CompiledMapPath compiled) {}
}
