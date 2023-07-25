/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features.overlays;

import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.resources.language.I18n;

public abstract class DynamicOverlay extends Overlay {
    private final int id;

    protected DynamicOverlay(OverlayPosition position, float width, float height, int id) {
        super(position, width, height);
        this.id = id;
    }

    protected DynamicOverlay(OverlayPosition position, OverlaySize size, int id) {
        super(position, size);
        this.id = id;
    }

    protected DynamicOverlay(
            OverlayPosition position,
            OverlaySize size,
            HorizontalAlignment horizontalAlignmentOverride,
            VerticalAlignment verticalAlignmentOverride,
            int id) {
        super(position, size, horizontalAlignmentOverride, verticalAlignmentOverride);
        this.id = id;
    }

    // Default constructor to be called when instantiating the overlay with reflection
    protected DynamicOverlay(int id) {
        super(
                new OverlayPosition(
                        0,
                        0,
                        VerticalAlignment.MIDDLE,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.MIDDLE),
                new OverlaySize(100f, 20f),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE);
        this.id = id;
    }

    @Override
    public String getTranslatedName() {
        return I18n.get(
                "feature.wynntils." + getDeclaringFeatureNameCamelCase() + ".overlay." + getNameCamelCase() + ".name",
                id);
    }

    @Override
    public String getConfigJsonName() {
        return super.getConfigJsonName() + id;
    }

    @Override
    public boolean shouldBeEnabled() {
        if (!isParentEnabled()) {
            return false;
        }

        if (this.isUserEnabled() != null) {
            return this.isUserEnabled();
        }

        // A dynamic overlay is enabled if the parent is enabled and the user has not disabled it
        return true;
    }

    public int getId() {
        return id;
    }
}
