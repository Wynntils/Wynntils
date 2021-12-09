package com.wynntils.fabric;

import com.wynntils.WynntilsMod;
import net.fabricmc.api.ModInitializer;

public class WynntilsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WynntilsMod.init();
    }
}
