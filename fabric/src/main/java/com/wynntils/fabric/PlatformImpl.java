/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric;

import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class PlatformImpl {
    public static String getModVersion() {
        Optional<ModContainer> wynntilsMod = FabricLoader.getInstance().getModContainer("wynntils");
        if (wynntilsMod.isEmpty()) {
            throw new RuntimeException("Wynntils does not exist???");
        }

        return wynntilsMod.get().getMetadata().getVersion().getFriendlyString();
    }
}
