/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

public class TomeProfile {
    private final String displayName;
    private final ItemTier itemTier;

    private final String variant;
    private final TomeType type;
    private final String tomeTier;

    public TomeProfile(String displayName, ItemTier itemTier, String variant, TomeType type, String tomeTier) {
        this.displayName = displayName;
        this.itemTier = itemTier;
        this.variant = variant;
        this.type = type;
        this.tomeTier = tomeTier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemTier getItemTier() {
        return itemTier;
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
                + displayName + "', itemTier="
                + itemTier + ", variant="
                + variant + ", type="
                + type + ", tomeTier="
                + tomeTier + '}';
    }
}
