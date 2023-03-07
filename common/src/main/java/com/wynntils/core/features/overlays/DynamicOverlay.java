/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.google.common.collect.ComparisonChain;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.overlays.sizes.OverlaySize;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.resources.language.I18n;

public abstract class DynamicOverlay extends Overlay {
    protected final int id;

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
                        VerticalAlignment.Middle,
                        HorizontalAlignment.Center,
                        OverlayPosition.AnchorSection.Middle),
                new GuiScaledOverlaySize(100f, 20f),
                HorizontalAlignment.Center,
                VerticalAlignment.Middle);
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

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(Overlay other) {
        return ComparisonChain.start()
                .compareTrueFirst(this.isParentEnabled(), other.isParentEnabled())
                .compare(
                        this.getDeclaringClass().getSimpleName(),
                        other.getDeclaringClass().getSimpleName())
                .compare(
                        this.getId(),
                        (other instanceof DynamicOverlay dynamicOverlay) ? dynamicOverlay.getId() : 0,
                        Integer::compareTo)
                .compare(this.getTranslatedName(), other.getTranslatedName())
                .result();
    }
}
