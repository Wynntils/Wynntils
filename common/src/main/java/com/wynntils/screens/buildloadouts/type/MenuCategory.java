/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.type;

import com.wynntils.services.loadout.type.LoadoutType;

public enum MenuCategory {
    BUILD_LOADOUT,
    ABILITY_TREE_LOADOUT,
    SKILL_POINT_LOADOUT,
    ASPECT_LOADOUT,
    NEW_LOADOUT;

    public LoadoutType getLoadoutType() {
        return switch (this) {
            case BUILD_LOADOUT -> LoadoutType.BUILD;
            case ABILITY_TREE_LOADOUT -> LoadoutType.ABILITY_TREE;
            case SKILL_POINT_LOADOUT -> LoadoutType.SKILL_POINT;
            case ASPECT_LOADOUT -> LoadoutType.ASPECT;
            case NEW_LOADOUT -> null;
        };
    }
}
