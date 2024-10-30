/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes;

import com.wynntils.services.mapdata.attributes.impl.MapMarkerOptionsImpl;
import com.wynntils.services.mapdata.attributes.impl.MapVisibilityImpl;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Optional;

/**
 * These are the fallback attributes used by FullFeatureAttribute if no other attributes
 * are defined. These are guaranteed to be non-empty.
 */
public final class DefaultMapAttributes implements MapAttributes {
    public static final MapVisibility ICON_ALWAYS = new MapVisibilityImpl(0f, 100f, 6f);
    public static final MapVisibility ICON_NEVER = new MapVisibilityImpl(100f, 0f, 6f);
    public static final MapVisibility LABEL_ALWAYS = new MapVisibilityImpl(0f, 100f, 3f);
    public static final MapVisibility LABEL_NEVER = new MapVisibilityImpl(100f, 0f, 3f);

    public static final MapMarkerOptions DEFAULT_MARKER_OPTIONS =
            new MapMarkerOptionsImpl(0f, 15000f, 3f, CommonColors.RED, true, true, true);

    public static final DefaultMapAttributes INSTANCE = new DefaultMapAttributes();

    private DefaultMapAttributes() {}

    @Override
    public Optional<Integer> getPriority() {
        return Optional.of(500);
    }

    @Override
    public Optional<Integer> getLevel() {
        return Optional.of(0);
    }

    @Override
    public Optional<String> getLabel() {
        return Optional.of("");
    }

    @Override
    public Optional<MapVisibility> getLabelVisibility() {
        return Optional.of(LABEL_ALWAYS);
    }

    @Override
    public Optional<CustomColor> getLabelColor() {
        return Optional.of(CommonColors.WHITE);
    }

    @Override
    public Optional<TextShadow> getLabelShadow() {
        return Optional.of(TextShadow.OUTLINE);
    }

    @Override
    public Optional<String> getIconId() {
        return Optional.of(MapIcon.NO_ICON_ID);
    }

    @Override
    public Optional<MapVisibility> getIconVisibility() {
        return Optional.of(ICON_ALWAYS);
    }

    @Override
    public Optional<CustomColor> getIconColor() {
        return Optional.of(CommonColors.WHITE);
    }

    @Override
    public Optional<MapDecoration> getIconDecoration() {
        return Optional.of(MapDecoration.NONE);
    }

    @Override
    public Optional<Boolean> getHasMarker() {
        return Optional.of(false);
    }

    @Override
    public Optional<MapMarkerOptions> getMarkerOptions() {
        return Optional.of(DEFAULT_MARKER_OPTIONS);
    }

    @Override
    public Optional<CustomColor> getFillColor() {
        return Optional.of(CommonColors.WHITE);
    }

    @Override
    public Optional<CustomColor> getBorderColor() {
        return Optional.of(CommonColors.WHITE);
    }

    @Override
    public Optional<Float> getBorderWidth() {
        return Optional.of(1.0f);
    }
}
