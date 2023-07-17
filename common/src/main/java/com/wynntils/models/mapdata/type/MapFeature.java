/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.type;

import com.wynntils.models.mapdata.style.MapFeatureStyle;
import java.util.List;

public interface MapFeature {
    String getId();

    MapCategory getCategory();

    MapFeatureStyle getStyle();

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
styles == list of named styles
icons == icon name -> base64 png representation
 */
