/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core;

import com.wynntils.core.features.Feature;
import com.wynntils.features.*;
import com.wynntils.features.debug.PacketDebuggerFeature;
import java.util.LinkedList;
import java.util.List;

/**
 * Loads {@link Feature}s
 */
public class FeatureLoader {
    private static final List<Feature> FEATURES = new LinkedList<>();

    private static void registerFeature(Feature feature) {
        FEATURES.add(feature);
    }

    private static void initializeFeatures() {
        FEATURES.forEach(Feature::onEnable);
    }

    public static List<Feature> getFeatures() {
        return FEATURES;
    }

    public static void init() {
        registerFeature(new WynncraftButtonFeature());
        registerFeature(new SoulPointTimerFeature());
        registerFeature(new ConnectionProgressFeature());
        registerFeature(new PacketDebuggerFeature());
        registerFeature(new ItemGuessFeature());

        initializeFeatures();
    }
}
