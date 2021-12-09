package com.wynntils.fabric;

import com.wynntils.ExampleMod;
import net.fabricmc.api.ModInitializer;

public class WynntilsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExampleMod.init();
    }
}
