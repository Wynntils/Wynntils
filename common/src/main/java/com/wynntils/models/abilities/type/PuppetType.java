/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import java.util.Locale;
import org.jspecify.annotations.Nullable;

public enum PuppetType {
    PUPPET,
    REMNANT,
    PATCHWORK_ABOMINATION;

    public static @Nullable PuppetType fromString(String type) {
        String sanitizedString = type.toUpperCase(Locale.ROOT).replace(' ', '_');

        try {
            return valueOf(sanitizedString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
