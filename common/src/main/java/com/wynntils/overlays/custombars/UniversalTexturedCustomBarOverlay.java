/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.custombars;

import com.wynntils.core.config.Config;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;

public class UniversalTexturedCustomBarOverlay extends CustomBarOverlayBase {
    @Persisted
    public final Config<CustomColor> color = new Config<>(CommonColors.WHITE);

    public UniversalTexturedCustomBarOverlay(int id) {
        super(id, new OverlaySize(81, 21));
    }

    @Override
    public CustomColor getRenderColor() {
        return color.get();
    }

    @Override
    public Texture getTexture() {
        return Texture.UNIVERSAL_BAR;
    }

    @Override
    protected float getTextureHeight() {
        return Texture.UNIVERSAL_BAR.height() / 2f;
    }

    @Override
    protected BarOverlayTemplatePair getActualPreviewTemplate() {
        return new BarOverlayTemplatePair("3/10", "capped(3; 10)");
    }
}
