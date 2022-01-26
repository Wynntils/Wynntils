package com.wynntils.core.features;

import java.util.ArrayList;
import java.util.List;

public abstract class FeatureLoader {
    protected final List<Feature> features = new ArrayList<>();

    public List<Feature> features() {
        return features;
    }

    public abstract void load();
}
