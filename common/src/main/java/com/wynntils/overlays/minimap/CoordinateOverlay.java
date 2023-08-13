/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.minimap;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class CoordinateOverlay extends TextOverlay {
    private static final String TEMPLATE = "{x(my_loc):0} {y(my_loc):0} {z(my_loc):0}";
    private static final String TEMPLATE_COLORED = "&c{x(my_loc):0} &a{y(my_loc):0} &9{z(my_loc):0}";

    @Persisted
    public final Config<Boolean> shouldBeColored = new Config<>(false);

    @Persisted
    public final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    public CoordinateOverlay() {
        super(
                new OverlayPosition(
                        136,
                        6,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.TOP_LEFT),
                new OverlaySize(130, 20),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE);
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Models.CharacterStats.hideCoordinates(!this.shouldDisplayOriginal.get());
    }

    @Override
    public String getTemplate() {
        return this.shouldBeColored.get() ? TEMPLATE_COLORED : TEMPLATE;
    }

    @Override
    public String getPreviewTemplate() {
        return this.shouldBeColored.get() ? TEMPLATE_COLORED : TEMPLATE;
    }
}
