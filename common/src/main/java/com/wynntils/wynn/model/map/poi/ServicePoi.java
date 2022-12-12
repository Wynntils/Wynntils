/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.gui.render.Texture;

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
