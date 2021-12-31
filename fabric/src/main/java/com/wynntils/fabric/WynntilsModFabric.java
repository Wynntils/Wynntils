/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric;

import com.wynntils.core.WynntilsMod;
import net.fabricmc.api.ClientModInitializer;

public class WynntilsModFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WynntilsMod.init();
    }
}
