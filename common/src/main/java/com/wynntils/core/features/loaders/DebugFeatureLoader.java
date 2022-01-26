package com.wynntils.core.features.loaders;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.features.FeatureLoader;
import com.wynntils.features.debug.ConnectionProgressFeature;
import com.wynntils.features.debug.KeyBindTestFeature;
import com.wynntils.features.debug.PacketDebuggerFeature;

public class DebugFeatureLoader extends FeatureLoader {
    {
        features.add(new PacketDebuggerFeature());
        features.add(new KeyBindTestFeature());
        features.add(new ConnectionProgressFeature());
    }

    @Override
    public void load() {
        if (WynntilsMod.developmentEnvironment) {
            FeatureRegistry.registerFeatures(features);
        }
    }
}
