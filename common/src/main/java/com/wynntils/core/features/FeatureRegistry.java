/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.features.loaders.DebugFeatureLoader;
import com.wynntils.core.features.loaders.DefaultFeatureLoader;

import java.util.LinkedList;
import java.util.List;

/** Loads {@link Feature}s */
public class FeatureRegistry {
    private static final List<Feature> FEATURES = new LinkedList<>();

    public static void registerFeature(Feature feature) {
        FEATURES.add(feature);
        feature.onEnable();
    }

    public static void registerFeatures(List<Feature> features) {
        FEATURES.addAll(features);
        features.forEach(Feature::onEnable);
    }

    public static List<Feature> getFeatures() {
        return FEATURES;
    }

    private static final DebugFeatureLoader debug = new DebugFeatureLoader();
    private static final DefaultFeatureLoader def = new DefaultFeatureLoader();

    public static void init() {
        debug.load();
        def.load();
    }
}
