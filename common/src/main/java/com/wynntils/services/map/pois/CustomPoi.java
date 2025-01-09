/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.Texture;
import java.util.Objects;

// The poi system is not used anymore. This special class is kept so upfixing old pois to mapdata is possible.
@Deprecated
public class CustomPoi {
    private final PoiLocation location;
    private final String name;
    private final CustomColor color;
    private final Texture icon;
    private final Visibility visibility;

    private CustomPoi(PoiLocation location, String name, CustomColor color, Texture icon, Visibility visibility) {
        this.location = location;
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.visibility = visibility;
    }

    @Deprecated
    public PoiLocation getLocation() {
        return location;
    }

    @Deprecated
    public String getName() {
        return name;
    }

    @Deprecated
    public CustomColor getColor() {
        return color;
    }

    @Deprecated
    public Texture getIcon() {
        return icon;
    }

    @Deprecated
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        CustomPoi customPoi = (CustomPoi) other;
        return location.equals(customPoi.location)
                && visibility == customPoi.visibility
                && name.equals(customPoi.name)
                && color.equals(customPoi.color)
                && icon == customPoi.icon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, name, color, icon, visibility);
    }

    public enum Visibility {
        DEFAULT("screens.wynntils.waypointCreation.visibility.default"),
        ALWAYS("screens.wynntils.waypointCreation.visibility.alwaysVisible"),
        HIDDEN("screens.wynntils.waypointCreation.visibility.hidden");

        private final String translationKey;

        Visibility(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }
}
