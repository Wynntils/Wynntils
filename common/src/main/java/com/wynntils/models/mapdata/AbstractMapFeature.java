package com.wynntils.models.mapdata;

import com.wynntils.utils.colors.CustomColor;
import java.util.List;

public class AbstractMapFeature implements MapFeature {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public MapVisibility getNameVisibility() {
        return null;
    }

    @Override
    public CustomColor getNameColor() {
        return null;
    }

    @Override
    public MapIcon getIcon() {
        return null;
    }

    @Override
    public MapVisibility getIconVisibility() {
        return null;
    }

    @Override
    public CustomColor getIconColor() {
        return null;
    }

    @Override
    public MapCategory getCategory() {
        return null;
    }

    @Override
    public List<String> getTags() {
        return null;
    }
}
