/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge;

import com.wynntils.ExampleExpectPlatform;
import java.nio.file.Path;
import net.minecraftforge.fml.loading.FMLPaths;

public class ExampleExpectPlatformImpl {
    /** This is our actual method to {@link ExampleExpectPlatform#getConfigDirectory()}. */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
