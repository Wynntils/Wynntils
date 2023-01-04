/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;
import com.wynntils.wynn.utils.WynnItemMatchers;
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
                new TextRenderTask(
                        icon,
                        TextRenderSetting.DEFAULT
                                .withCustomColor(color)
                                .withTextShadow(ItemTextOverlayFeature.INSTANCE.skillIconShadow)),
                -1,
                1,
                0.9f);
    }

    @Override
    public TextOverlay getTextOverlay() {
        return textOverlay;
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return ItemTextOverlayFeature.INSTANCE.skillIconEnabled;
    }
}
