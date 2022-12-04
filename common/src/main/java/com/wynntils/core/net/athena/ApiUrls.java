/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.athena;

import com.wynntils.core.managers.CoreManager;

/**
 * Class that parses a String into a specific format of "[Key] = Value"
 *
 * <p>Ex: https://api.wynntils.com/webapi provides such a format
 */
public class ApiUrls extends CoreManager {
    public static void init() {
    }

    public static boolean isSetup() {
        return true;
    }

}
