/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.map.pois;

import com.wynntils.core.components.Managers;
import com.wynntils.features.map.MapFeature;
import com.wynntils.models.map.PoiLocation;
import com.wynntils.models.map.type.DisplayPriority;
import com.wynntils.models.map.type.ServiceKind;
import com.wynntils.utils.render.Texture;

public class ServicePoi extends StaticIconPoi {
    private final ServiceKind kind;

    public ServicePoi(PoiLocation location, ServiceKind kind) {
        super(location);
        this.kind = kind;
    }

    @Override
    public Texture getIcon() {
        return kind.getIcon();
    }

    @Override
    public float getMinZoomForRender() {
        if (kind == ServiceKind.FAST_TRAVEL) {
            return Managers.Feature.getFeatureInstance(MapFeature.class)
                    .fastTravelPoiMinZoom
                    .get();
        } else {
            return Managers.Feature.getFeatureInstance(MapFeature.class)
                    .servicePoiMinZoom
                    .get();
        }
    }

    @Override
    public String getName() {
        return kind.getName();
    }

    public ServiceKind getKind() {
        return kind;
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.LOWEST;
    }
}
