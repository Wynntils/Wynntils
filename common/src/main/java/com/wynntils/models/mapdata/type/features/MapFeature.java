/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.type.features;

import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import java.util.List;

public interface MapFeature {
    // Required. The id should be unique, and track the provenance of the feature
    String getFeatureId();

    // Required.
    String getCategoryId();

    // Optional
    MapFeatureAttributes getAttributes();

    // Optional
    List<String> getTags();
}
/*
style application:
1) root style
2) category style, starting at top category and letting most specific category override
3) the feature's own style, starting at top category and letting most specific category override

json files can contain:
features == list of concrete map features (locations, paths or areas)
categories == list of category definitions
icons == icon name -> base64 png representation
 */

// wynntils:lootrun:chest:tier1
// wynntils:service:profession:scribing
// wynntils:service:identifier
// wynntils:npc:quest
// wynntils:personal:lootrunpath
// wynntils:personal:openedchest:tier3
// wynntils:personal:discovery:territory
// wynntils:personal:saved_bookmarks_poi ???
