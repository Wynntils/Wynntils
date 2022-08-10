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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public class SkillPointProperty extends CustomStackCountProperty {
    private static final Pattern POINT_PATTERN = Pattern.compile("^.+?(\\d+) points .+$");

    public SkillPointProperty(WynnItemStack item) {
        super(item);

        char colorCode = ComponentUtils.getCoded(item.getHoverName()).charAt(16);
        String points = "";
        for (String lore : ItemUtils.getLore(item)) {
            Matcher m = POINT_PATTERN.matcher(lore);
            if (m.find()) {
                points = m.group(1);
                break;
            }
        }

        this.setCustomStackCount(
                points,
                CustomColor.fromChatFormatting(Objects.requireNonNull(ChatFormatting.getByCode(colorCode))),
                FontRenderer.TextShadow.NORMAL);
    }
}
