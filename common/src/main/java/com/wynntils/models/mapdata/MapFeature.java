package com.wynntils.models.mapdata;

import com.wynntils.utils.colors.CustomColor;
import java.util.List;

public interface MapFeature {
    String getName();
    MapVisibility getNameVisibility();
    CustomColor getNameColor();

    MapIcon getIcon();
    MapVisibility getIconVisibility();
    CustomColor getIconColor();

    MapCategory getCategory();
    List<String> getTags();
}
