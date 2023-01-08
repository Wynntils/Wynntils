/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

public class TomeProfile {
    private final String displayName;
    private final GearTier gearTier;

    private final String variant;
    private final TomeType type;
    private final String tomeTier;

    public TomeProfile(String displayName, GearTier gearTier, String variant, TomeType type, String tomeTier) {
        this.displayName = displayName;
        this.gearTier = gearTier;
        this.variant = variant;
        this.type = type;
        this.tomeTier = tomeTier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public GearTier getGearTier() {
        return gearTier;
    }

    public String getVariant() {
        return variant;
    }

    public TomeType getType() {
        return type;
    }

    public String getTomeTier() {
        return tomeTier;
    }

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
