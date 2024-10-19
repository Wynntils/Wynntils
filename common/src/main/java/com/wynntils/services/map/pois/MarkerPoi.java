/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.Texture;

public class MarkerPoi extends StaticIconPoi {
    private final String name;
    private final Texture texture;

    public MarkerPoi(PoiLocation location, String name, Texture texture) {
        super(location);
        this.name = name;
        this.texture = texture;
    }

    @Override
    public Texture getIcon() {
        return texture;
    }

    @Override
    protected float getMinZoomForRender() {
        return -1f;
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.NORMAL;
    }

    @Override
    public String getName() {
        return name;
    }
}
