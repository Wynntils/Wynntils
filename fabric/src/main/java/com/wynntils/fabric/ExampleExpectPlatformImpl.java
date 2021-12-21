/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric;

import com.wynntils.ExampleExpectPlatform;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class ExampleExpectPlatformImpl {
    /** This is our actual method to {@link ExampleExpectPlatform#getConfigDirectory()}. */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
