/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.minimap;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class TerritoryOverlay extends TextOverlay {
    @Persisted
    private final Config<TerritoryOwnerDisplay> showOwner = new Config<>(TerritoryOwnerDisplay.HIDE);

    public TerritoryOverlay() {
        super(
                new OverlayPosition(
                        140 + McUtils.mc().font.lineHeight,
                        6,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.TOP_LEFT),
                new OverlaySize(130, 40),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE);
    }

    @Override
    protected String getTemplate() {
        return showOwner.get().getTemplate();
    }

    @Override
    public String getPreviewTemplate() {
        return getTemplate();
    }

    private enum TerritoryOwnerDisplay {
        NAME("{territory}\n{territory_owner}"),
        TAG("{if_str(eq_str(territory;\"\");\"\";concat(territory;\" [\";territory_owner(true);\"]\"))}"),
        HIDE("{territory}");

        private final String template;

        TerritoryOwnerDisplay(String template) {
            this.template = template;
        }

        private String getTemplate() {
            return template;
        }
    }
}
