/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.gui.render.Texture;

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
