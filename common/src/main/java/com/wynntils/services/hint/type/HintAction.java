/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hint.type;

public enum HintAction {
    KEYBIND,
    TOGGLE_COMMAND,
    WYNNTILS_COMMAND;

    public static HintAction fromString(String name) {
        for (HintAction type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
