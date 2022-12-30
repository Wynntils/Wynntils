/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;

public abstract class CustomStackCountProperty extends ItemProperty implements TextOverlayProperty {
    private ItemTextOverlayFeature.TextOverlay textOverlay;

    protected CustomStackCountProperty(WynnItemStack item) {
        super(item);

        item.setCount(1);
    }

    protected void setCustomStackCount(String value, CustomColor color, FontRenderer.TextShadow shadow) {
        textOverlay = new ItemTextOverlayFeature.TextOverlay(
                new TextRenderTask(
                        value,
                        TextRenderSetting.DEFAULT
                                .withCustomColor(color)
                                .withHorizontalAlignment(HorizontalAlignment.Right)
                                .withTextShadow(shadow)),
                17,
                9,
                1);
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return true;
    }

    @Override
    public ItemTextOverlayFeature.TextOverlay getTextOverlay() {
        return textOverlay;
    }
}
