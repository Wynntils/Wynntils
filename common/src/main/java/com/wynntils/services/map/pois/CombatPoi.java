/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.core.components.Managers;
import com.wynntils.features.map.MapFeature;
import com.wynntils.services.map.type.CombatKind;
import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.Texture;

public class CombatPoi extends StaticIconPoi {
    private final String name;
    private final CombatKind kind;

    public CombatPoi(PoiLocation location, String name, CombatKind kind) {
        super(location);
        this.name = name;
        this.kind = kind;
    }

    @Override
    public Texture getIcon() {
        return kind.getIcon();
    }

    @Override
    public float getMinZoomForRender() {
        if (kind == CombatKind.CAVES) {
            return Managers.Feature.getFeatureInstance(MapFeature.class)
                    .cavePoiMinZoom
                    .get();
        }
        return Managers.Feature.getFeatureInstance(MapFeature.class)
                .combatPoiMinZoom
                .get();
    }

    @Override
    public String getName() {
        return name;
    }

    public CombatKind getKind() {
        return kind;
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.NORMAL;
    }
}
