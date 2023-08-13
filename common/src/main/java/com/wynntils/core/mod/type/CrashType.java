/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.mod.type;

public enum CrashType {
    ANNOTATOR("Item Annotator"),
    FUNCTION("Function"),
    FEATURE("Feature"),
    OVERLAY("Overlay"),
    KEYBIND("Key Bind"),
    SCREEN("Screen");

    private final String name;

    CrashType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
