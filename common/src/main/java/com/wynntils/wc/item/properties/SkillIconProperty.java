/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.item.properties;

import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.wc.item.WynnItemStack;
import com.wynntils.wc.item.parsers.WynnItemMatchers;
import com.wynntils.wc.item.properties.type.TextOverlayProperty;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;

public class SkillIconProperty extends ItemProperty implements TextOverlayProperty {
    private final TextOverlay textOverlay;

    public SkillIconProperty(WynnItemStack item) {
        super(item);
        String icon = "";
        CustomColor color = CustomColor.NONE;

        Matcher matcher = WynnItemMatchers.skillIconMatcher(item.getHoverName());
        if (matcher.matches()) {
            icon = matcher.group(2);
            color = CustomColor.fromChatFormatting(
                    ChatFormatting.getByCode(matcher.group(1).charAt(0)));
        }

        textOverlay = new TextOverlay(
                icon, color, FontRenderer.TextAlignment.LEFT_ALIGNED, FontRenderer.TextShadow.NORMAL, -1, 1, .75f);
    }

    @Override
    public TextOverlay getTextOverlay() {
        return textOverlay;
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return true;
    }

    @Override
    public boolean isHotbarText() {
        return true;
    }
}
