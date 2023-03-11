/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.event;

import com.wynntils.core.features.Feature;
import net.minecraftforge.eventbus.api.Event;

public abstract class FeatureStateChangeEvent extends Event {
    private final Feature feature;

    protected FeatureStateChangeEvent(Feature feature) {
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }

    public static class Enabled extends FeatureStateChangeEvent {
        public Enabled(Feature feature) {
            super(feature);
        }
    }

    public static class Disabled extends FeatureStateChangeEvent {
        public Disabled(Feature feature) {
            super(feature);
        }
    }

    public static class Crashed extends FeatureStateChangeEvent {
        public Crashed(Feature feature) {
            super(feature);
        }
    }
}
