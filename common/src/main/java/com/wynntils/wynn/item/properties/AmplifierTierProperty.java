/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.core.webapi.profiles.item.ItemTier;
import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;
import java.util.regex.Matcher;

public class AmplifierTierProperty extends ItemProperty implements TextOverlayProperty {
    private final TextOverlay textOverlay;

    public AmplifierTierProperty(WynnItemStack item) {
        super(item);

        // parse tier
        String ampNumeral = "I";
        Matcher ampMatcher = WynnItemMatchers.amplifierNameMatcher(item.getHoverName());
        if (ampMatcher.matches()) {
            ampNumeral = ampMatcher.group(1);
        }

        String text = ItemTextOverlayFeature.amplifierTierRomanNumerals
                ? ampNumeral
                : String.valueOf(MathUtils.integerFromRoman(ampNumeral));

        textOverlay = new TextOverlay(
                text,
                ItemTier.LEGENDARY.getHighlightColor(),
                FontRenderer.TextAlignment.LEFT_ALIGNED,
                ItemTextOverlayFeature.amplifierTierShadow,
                -1,
                1,
                0.75f);
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return ItemTextOverlayFeature.amplifierTierEnabled;
    }

    @Override
    public TextOverlay getTextOverlay() {
        return textOverlay;
    }
}
