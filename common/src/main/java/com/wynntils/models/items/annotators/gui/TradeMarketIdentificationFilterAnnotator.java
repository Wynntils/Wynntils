/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.TradeMarketIdentificationFilterItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class TradeMarketIdentificationFilterAnnotator implements GuiItemAnnotator {
    private static final Pattern IDENTIFICATION_FILTER_NAME = Pattern.compile("§a(?<statName>.+) Identification Only");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(IDENTIFICATION_FILTER_NAME);
        if (!matcher.matches()) return null;

        return new TradeMarketIdentificationFilterItem(matcher.group("statName"));
    }
}
