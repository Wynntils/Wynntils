/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.features.*;
import com.wynntils.features.debug.ConnectionProgressFeature;
import com.wynntils.features.debug.KeyBindTestFeature;
import com.wynntils.features.debug.PacketDebuggerFeature;
import java.util.LinkedList;
import java.util.List;

/** Loads {@link Feature}s */
public class FeatureRegistry {
    private static final List<Feature> FEATURES = new LinkedList<>();

    public static void registerFeature(Feature feature) {
        FEATURES.add(feature);
    }

    public static void registerFeatures(List<Feature> features) {
        FEATURES.addAll(features);
    }

    public static List<Feature> getFeatures() {
        return FEATURES;
    }

    public static void init() {
        // debug
        registerFeature(new PacketDebuggerFeature());
        registerFeature(new KeyBindTestFeature());
        registerFeature(new ConnectionProgressFeature());

        registerFeature(new WynncraftButtonFeature());
        registerFeature(new SoulPointTimerFeature());
        registerFeature(new ItemGuessFeature());
        registerFeature(new GammabrightFeature());
        registerFeature(new HealthPotionBlockerFeature());
    }
}
