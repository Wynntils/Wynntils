/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

public record CharmProfile(String displayName, GearTier tier, String type) {

    @Override
    public String toString() {
        return "CharmProfile{" + "displayName='" + displayName + "', tier=" + tier + ", type=" + type + '}';
    }
}
