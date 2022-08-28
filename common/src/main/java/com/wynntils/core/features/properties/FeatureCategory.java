package com.wynntils.core.features.properties;

import java.util.Locale;
import net.minecraft.client.resources.language.I18n;

public enum FeatureCategory {
    UNCATEGORIZED,
    INVENTORY,
    OVERLAYS,
    PLAYERS,
    REDIRECTS,
    TOOLTIPS;

    @Override
    public String toString() {
        return I18n.get("core.wynntils.category." + this.name().toLowerCase(Locale.ROOT));
    }
}
