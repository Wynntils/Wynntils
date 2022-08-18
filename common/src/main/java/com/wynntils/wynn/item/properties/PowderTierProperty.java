/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.features.user.inventory.ItemHighlightFeature;
import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.type.HighlightProperty;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;

public class PowderTierProperty extends ItemProperty implements HighlightProperty, TextOverlayProperty {
    private final CustomColor highlightColor;

    private final TextOverlay textOverlay;

    public PowderTierProperty(WynnItemStack item) {
        super(item);

        // parse color
        ChatFormatting chatColor =
                ChatFormatting.getByCode(item.getHoverName().getString().charAt(1));
        if (chatColor == null) chatColor = ChatFormatting.WHITE;

        highlightColor = CustomColor.fromChatFormatting(chatColor);

        // parse tier
        Matcher powderMatcher = WynnItemMatchers.powderNameMatcher(item.getHoverName());
        String powderNumeral = "I";
        if (powderMatcher.matches()) {
            powderNumeral = powderMatcher.group(2);
        }

        // convert from roman to arabic if necessary
        String text = ItemTextOverlayFeature.powderTierRomanNumerals
                ? powderNumeral
                : String.valueOf(MathUtils.integerFromRoman(powderNumeral));

        textOverlay = new TextOverlay(
                text,
                highlightColor,
                FontRenderer.TextAlignment.LEFT_ALIGNED,
                ItemTextOverlayFeature.powderTierShadow,
                -1,
                1,
                0.75f);
    }

    @Override
    public boolean isHighlightEnabled() {
        return ItemHighlightFeature.powderHighlightEnabled;
    }

    @Override
    public CustomColor getHighlightColor() {
        return highlightColor;
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return ItemTextOverlayFeature.powderTierEnabled;
    }

    @Override
    public TextOverlay getTextOverlay() {
        return textOverlay;
    }
}
