/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.custom.item.WynnItemStack;
import com.wynntils.wc.custom.item.properties.type.TextOverlayProperty;
import net.minecraft.ChatFormatting;

public class SkillPointProperty extends ItemProperty implements TextOverlayProperty {
    private static final CustomColor TEXT_COLOR = CustomColor.fromChatFormatting(ChatFormatting.WHITE);
    private final TextOverlay textOverlay;

    public SkillPointProperty(WynnItemStack item) {
        super(item);
        item.setCount(1);

        // Current skill point amount is always on line 4 i.e. index 3.
        String pointsLine = ItemUtils.getLore(item).get(3);
        String points = pointsLine.substring(6, pointsLine.indexOf('p') - 1);

        textOverlay = new TextOverlay(
                points, TEXT_COLOR, FontRenderer.TextAlignment.RIGHT_ALIGNED, FontRenderer.TextShadow.NORMAL, 17, 9, 1);
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
