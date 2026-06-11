/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.overlays.minimap.CoordinatesOverlay;
import com.wynntils.overlays.minimap.MinimapOverlay;
import com.wynntils.overlays.minimap.TerritoryOverlay;
import com.wynntils.services.hades.type.PlayerRelation;
import com.wynntils.services.map.pois.PlayerMiniMapPoi;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.utils.type.RenderElementType;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;
import java.util.stream.Stream;

@ConfigCategory(Category.MAP)
public class MinimapFeature extends Feature {
    @RegisterOverlay
    public final MinimapOverlay minimapOverlay = new MinimapOverlay();

    @RegisterOverlay(renderType = RenderElementType.ACTION_BAR)
    private final Overlay coordinatesOverlay = new CoordinatesOverlay();

    @RegisterOverlay
    private final Overlay territoryOverlay = new TerritoryOverlay();

    @RegisterKeyBind
    public final KeyBind zoomIn = KeyBindDefinition.MINIMAP_ZOOM_IN.create(() -> minimapOverlay.adjustZoomLevel(2));

    @RegisterKeyBind
    public final KeyBind zoomOut = KeyBindDefinition.MINIMAP_ZOOM_OUT.create(() -> minimapOverlay.adjustZoomLevel(-2));

    private List<? extends Poi> visiblePois = List.of();

    public MinimapFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        Stream<? extends Poi> visiblePoiStream = Services.Poi.getServicePois();
        visiblePoiStream = Stream.concat(visiblePoiStream, Services.Poi.getCombatPois());
        visiblePoiStream = Stream.concat(
                visiblePoiStream, Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois.get().stream());
        visiblePoiStream = Stream.concat(visiblePoiStream, Services.Poi.getProvidedCustomPois().stream());
        visiblePoiStream = Stream.concat(visiblePoiStream, Models.Marker.getAllPois());
        visiblePoiStream = Stream.concat(visiblePoiStream, minimapOverlay.getMiniPlayerPois());
        visiblePoiStream = Stream.concat(
                visiblePoiStream, Services.Poi.getGatheringNodePois().filter(Services.Poi::isGatheringNodeTypeVisible));

        this.visiblePois = visiblePoiStream.toList();
    }

    public List<? extends Poi> getVisiblePois() {
        return visiblePois;
    }
}
