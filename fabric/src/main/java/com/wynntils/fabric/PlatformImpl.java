package com.wynntils.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

public class PlatformImpl {
    public static String getModVersion() {
        Optional<ModContainer> wynntilsMod = FabricLoader.getInstance().getModContainer("wynntils");
        if (wynntilsMod.isEmpty()) {
            throw new RuntimeException("Wynntils does not exist???");
        }

        return wynntilsMod.get().getMetadata().getVersion().getFriendlyString();
    }
}
