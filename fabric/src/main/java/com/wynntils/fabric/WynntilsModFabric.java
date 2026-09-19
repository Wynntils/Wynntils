/*
 * Copyright © Wynntils 2021-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric;

import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.BugReportUtils;
import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class WynntilsModFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BugReportUtils.registerPlatform(
                () -> FabricLoader.getInstance().getAllMods().stream()
                        .map(mod -> mod.getMetadata().getId() + " "
                                + mod.getMetadata().getVersion().getFriendlyString())
                        .sorted()
                        .collect(Collectors.joining("\n")),
                () -> FabricLoader.getInstance()
                        .getModContainer("fabricloader")
                        .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                        .orElse("Unknown"));
        Optional<ModContainer> wynntilsMod = FabricLoader.getInstance().getModContainer("wynntils");
        if (wynntilsMod.isEmpty()) {
            WynntilsMod.error("Where is my Wynntils? :(");
            return;
        }

        WynntilsMod.init(
                WynntilsMod.ModLoader.FABRIC,
                wynntilsMod.get().getMetadata().getVersion().getFriendlyString(),
                FabricLoader.getInstance().isDevelopmentEnvironment(),
                new File(wynntilsMod.get().getOrigin().getPaths().getFirst().toUri()));
    }
}
