/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.utils.StringUtils;
import java.util.Optional;

public enum TomeType {
    WEAPON(true, true),
    ARMOUR(true, true),
    GUILD("Allegiance", true, false),
    SLAYING(false, true),
    GATHERING(false, true),
    DUNGEONEERING(false, true),
    LOOTRUN(true, false);

    private final String typeString;
    private final boolean hasVariants;
    private final boolean isTiered;

    TomeType(boolean hasVariants, boolean isTiered) {
        this.typeString = StringUtils.capitalized(this.name());
        this.hasVariants = hasVariants;
        this.isTiered = isTiered;
    }

    TomeType(String typeString, boolean hasVariants, boolean isTiered) {
        this.typeString = typeString;
        this.hasVariants = hasVariants;
        this.isTiered = isTiered;
    }

    public String getName() {
        return typeString;
    }

    public boolean hasVariants() {
        return hasVariants;
    }

    public boolean isTiered() {
        return isTiered;
    }

    public static Optional<TomeType> fromString(String typeStr) {
        for (TomeType type : values()) {
            if (type.typeString.equals(typeStr)) return Optional.of(type);
        }
        return Optional.empty();
    }
}
