/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.custom.item.WynnItemStack;
import java.util.Objects;
import net.minecraft.ChatFormatting;

public class SkillPointProperty extends CustomStackCountProperty {
    public SkillPointProperty(WynnItemStack item) {
        super(item);

        try {
            char colorCode = ComponentUtils.getCoded(item.getHoverName()).charAt(16);
            // Current skill point amount is always on line 4 i.e. index 3.
            String pointsLine = ItemUtils.getLore(item).get(3);
            String points = pointsLine.substring(6, pointsLine.indexOf('p') - 1);

            this.setCustomStackCount(
                    points,
                    CustomColor.fromChatFormatting(Objects.requireNonNull(ChatFormatting.getByCode(colorCode))),
                    FontRenderer.TextShadow.NORMAL);
        } catch (IndexOutOfBoundsException | NullPointerException ignored) {
        }
    }
}
