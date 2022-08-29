/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.core.WynntilsMod;
import java.io.File;

public final class FileUtils {
    /**
     * Wraps File#mkdirs with a log output, in case of failure
     */
    public static void mkdir(File dir) {
        if (dir.isDirectory()) return;

        if (!dir.mkdirs()) {
            WynntilsMod.error("Directory " + dir + " could not be created");
        }
    }
}
