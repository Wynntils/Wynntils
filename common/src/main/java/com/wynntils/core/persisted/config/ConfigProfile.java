/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.config;

import java.util.Locale;
import net.minecraft.client.resources.language.I18n;

public enum ConfigProfile {
    DEFAULT,
    NEW_PLAYER,
    LITE,
    MINIMAL,
    BLANK_SLATE;

    public String getShortDescription() {
        return I18n.get("core.wynntils.profile." + this.name().toLowerCase(Locale.ROOT) + ".shortDescription");
    }

    public String getDescription() {
        return I18n.get("core.wynntils.profile." + this.name().toLowerCase(Locale.ROOT) + ".description");
    }
}
