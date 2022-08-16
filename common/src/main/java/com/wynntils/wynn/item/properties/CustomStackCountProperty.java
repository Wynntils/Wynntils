/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;

public abstract class CustomStackCountProperty extends ItemProperty implements TextOverlayProperty {
    private TextOverlay textOverlay;

    protected CustomStackCountProperty(WynnItemStack item) {
        super(item);

        item.setCount(1);
    }

    protected void setCustomStackCount(String value, CustomColor color, FontRenderer.TextShadow shadow) {
        textOverlay = new TextOverlay(value, color, FontRenderer.TextAlignment.RIGHT_ALIGNED, shadow, 17, 9, 1);
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return true;
    }

    @Override
    public TextOverlay getTextOverlay() {
        return textOverlay;
    }
}
