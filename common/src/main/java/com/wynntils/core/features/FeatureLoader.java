/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
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
