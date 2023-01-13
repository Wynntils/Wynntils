/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

public record TomeProfile(String displayName, GearTier gearTier, String variant, TomeType type, String tomeTier) {

    @Override
    public String toString() {
        return "TomeProfile{" + "displayName='"
                + displayName + "', gearTier="
                + gearTier + ", variant="
                + variant + ", type="
                + type + ", tomeTier="
                + tomeTier + '}';
    }
}
