/*
 * Copyright © Wynntils 2024.
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

public class TowerVolleyTimerOverlay extends TextOverlay {
    private static final String TEMPLATE =
            "{if_string(gte(volley_timer; 0); concat(\"Volley: : \"; string(volley_timer:1); \"s\"); \"\")}";

    @Persisted
    public final Config<CustomColor> textColor = new Config<>(CommonColors.MAGENTA);

    public TowerVolleyTimerOverlay() {
        super(
                new OverlayPosition(
                        30, 0, VerticalAlignment.TOP, HorizontalAlignment.CENTER, OverlayPosition.AnchorSection.MIDDLE),
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
        return "Volley: 3.2s";
    }
}
