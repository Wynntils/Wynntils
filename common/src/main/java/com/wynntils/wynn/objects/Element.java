/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import com.wynntils.utils.StringUtils;

public enum Element {
    AIR,
    EARTH,
    FIRE,
    THUNDER,
    WATER;

    private final String displayName;

    Element() {
        this.displayName = StringUtils.capitalized(this.name());
    }

    public String getDisplayName() {
        return displayName;
    }
}
