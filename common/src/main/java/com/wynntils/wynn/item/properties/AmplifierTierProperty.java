/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
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

        String text = ItemTextOverlayFeature.INSTANCE.amplifierTierRomanNumerals
                ? ampNumeral
                : String.valueOf(MathUtils.integerFromRoman(ampNumeral));

        textOverlay = new TextOverlay(
                new TextRenderTask(
                        text,
                        TextRenderSetting.DEFAULT
                                .withCustomColor(ItemTier.LEGENDARY.getHighlightColor())
                                .withTextShadow(ItemTextOverlayFeature.INSTANCE.amplifierTierShadow)),
                -1,
                1,
                0.75f);
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return ItemTextOverlayFeature.INSTANCE.amplifierTierEnabled;
    }

    @Override
    public TextOverlay getTextOverlay() {
        return textOverlay;
    }
}
