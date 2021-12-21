/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric;

import com.wynntils.WynntilsMod;
import net.fabricmc.api.ModInitializer;

public class WynntilsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WynntilsMod.init();
    }
}
