package com.wynntils.models.mapdata;

import com.wynntils.utils.mc.type.Location;
import java.util.List;

public abstract class MapPath extends AbstractMapFeature {
    public abstract List<Location> getLocation();

}
