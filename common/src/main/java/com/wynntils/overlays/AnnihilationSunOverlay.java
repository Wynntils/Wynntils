/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class AnnihilationSunOverlay extends TextOverlay {
    private static final String TEMPLATE =
            "{if_str(at_cap(annihilation_sun_progress);\"\";concat(\"Sun Forming: \";str(curr(annihilation_sun_progress));\"%\"))}";

    @Persisted
    private final Config<CustomColor> textColor = new Config<>(CommonColors.RED);

    public AnnihilationSunOverlay() {
        super(
                new OverlayPosition(
                        0, 0, VerticalAlignment.TOP, HorizontalAlignment.CENTER, OverlayPosition.AnchorSection.MIDDLE),
                new OverlaySize(150, 30),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE);
        fontScale.store(2f);
    }

    @Override
    public CustomColor getRenderColor() {
        return textColor.get();
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public String getPreviewTemplate() {
        return "Sun Forming: 50%";
    }
}
