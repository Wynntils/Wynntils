/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.core.components.Managers;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.services.mapdata.features.ServiceLocation;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.Texture;

public class ServicePoi extends StaticIconPoi {
    private final ServiceLocation.ServiceKind kind;

    public ServicePoi(PoiLocation location, ServiceLocation.ServiceKind kind) {
        super(location);
        this.kind = kind;
    }

    @Override
    public Texture getIcon() {
        return kind.getIcon();
    }

    @Override
    public float getMinZoomForRender() {
        if (kind == ServiceLocation.ServiceKind.FAST_TRAVEL) {
            return Managers.Feature.getFeatureInstance(MainMapFeature.class)
                    .fastTravelPoiMinZoom
                    .get();
        } else {
            return Managers.Feature.getFeatureInstance(MainMapFeature.class)
                    .servicePoiMinZoom
                    .get();
        }
    }

    @Override
    public String getName() {
        return kind.getName();
    }

    public ServiceLocation.ServiceKind getKind() {
        return kind;
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.LOWEST;
    }
}
