/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts.type;

import java.util.Locale;

public enum TooltipBannerIcon {
    EMERALD,
    ACTIVE_FLAME,
    UNTRADABLE,
    QUEST,
    INACTIVE_FLAME,
    REROLL,
    EARTH,
    THUNDER,
    WATER,
    FIRE,
    AIR;

    public String getKey() {
        return name().toLowerCase(Locale.ROOT);
    }
}
