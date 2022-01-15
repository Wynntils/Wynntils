/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils;

import dev.architectury.injectables.annotations.ExpectPlatform;

// This is a bad place to have it, but I can't find a better place for it to be put and not break
// everything
public class Platform {
    @ExpectPlatform
    public static String getModVersion() {
        throw new AssertionError();
    }
}
