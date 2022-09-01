/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.core.WynntilsMod;
import java.io.File;
import java.io.IOException;

public final class FileUtils {
    /**
     * Wraps File#mkdirs with a log output, in case of failure
     */
    public static void mkdir(File dir) {
        if (dir.isDirectory()) {
            assert false;
            return;
        }

        if (!dir.mkdirs()) {
            WynntilsMod.error("Directory " + dir + " could not be created");
        }
    }

    /**
     * Wraps File#mkdirs with a log output, in case of failure
     */
    public static void createNewFile(File file) {
        if (file.isFile()) {
            assert false;
            return;
        }

        try {
            if (!file.createNewFile()) {
                WynntilsMod.error("File " + file + " could not be created");
            }
        } catch (IOException e) {
            WynntilsMod.error("IOException while created File " + file);
        }
    }
}
