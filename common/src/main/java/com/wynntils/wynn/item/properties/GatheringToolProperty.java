/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;
import com.wynntils.wynn.utils.WynnItemMatchers;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;

public class GatheringToolProperty extends ItemProperty implements TextOverlayProperty {
    private final TextOverlay textOverlay;

    public GatheringToolProperty(WynnItemStack item) {
        super(item);

        // parse tier
        Matcher emeraldPouchMatcher = WynnItemMatchers.gatheringToolMatcher(item.getHoverName());
        String numeral = emeraldPouchMatcher.matches() ? emeraldPouchMatcher.group(2) : "1";

        // convert from roman to arabic if necessary
        String text = ItemTextOverlayFeature.INSTANCE.gatheringToolTierRomanNumerals
                ? MathUtils.toRoman(Integer.parseInt(numeral))
                : numeral;

        TextRenderSetting style = TextRenderSetting.DEFAULT
                .withCustomColor(CustomColor.fromChatFormatting(ChatFormatting.DARK_AQUA))
                .withTextShadow(ItemTextOverlayFeature.INSTANCE.gatheringToolTierShadow);

        textOverlay = new TextOverlay(new TextRenderTask(text, style), -1, 1, 0.9f);
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return ItemTextOverlayFeature.INSTANCE.gatheringToolTierEnabled;
    }

    @Override
    public TextOverlay getTextOverlay() {
        return textOverlay;
    }
}
