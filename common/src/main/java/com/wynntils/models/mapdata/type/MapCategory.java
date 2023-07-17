/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.type;

import com.wynntils.models.mapdata.style.MapFeatureStyle;

// wynntils:lootrun:chest:tier1
// wynntils:service:profession:scribing
// wynntils:service:identifier
// wynntils:npc:quest
// wynntils:personal:lootrunpath
// wynntils:personal:openedchest:tier3
// wynntils:personal:discovery:territory
// wynntils:personal:saved_bookmarks_poi ???

public record MapCategory(MapCategory parent, String name, String displayName, MapFeatureStyle style) {
    public String getPath() {
        return (parent != null ? parent.getPath() + ":" : "") + name;
    }
}
