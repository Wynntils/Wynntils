package com.wynntils.core.features.loaders;

import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.features.FeatureLoader;
import com.wynntils.features.GammabrightFeature;
import com.wynntils.features.ItemGuessFeature;
import com.wynntils.features.SoulPointTimerFeature;
import com.wynntils.features.WynncraftButtonFeature;

public class DefaultFeatureLoader extends FeatureLoader {
    {
        features.add(new WynncraftButtonFeature());
        features.add(new SoulPointTimerFeature());
        features.add(new ItemGuessFeature());
        features.add(new GammabrightFeature());
    }

    @Override
    public void load() {
        FeatureRegistry.registerFeatures(features);
    }
}
