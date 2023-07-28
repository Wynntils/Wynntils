/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import java.util.Locale;
import net.minecraft.client.resources.language.I18n;

public enum Category {
    UNCATEGORIZED,
    CHAT,
    COMBAT,
    COMMANDS,
    DEBUG,
    EMBELLISHMENTS,
    INVENTORY,
    MAP,
    OVERLAYS,
    PLAYERS,
    REDIRECTS,
    TOOLTIPS,
    TRADEMARKET,
    UI,
    UTILITIES,
    WYNNTILS;

    Category() {
        assert !toString().startsWith("core.wynntils");
    }

    @Override
    public String toString() {
        return I18n.get("core.wynntils.category." + this.name().toLowerCase(Locale.ROOT));
    }
}
