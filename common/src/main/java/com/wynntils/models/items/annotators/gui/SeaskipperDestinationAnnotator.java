/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.gui.SeaskipperDestinationItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class SeaskipperDestinationAnnotator implements ItemAnnotator {
    private static final Pattern SEASKIPPER_PASS_PATTERN = Pattern.compile("^§b(.*) Pass §7for §b(\\d+)²$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(SEASKIPPER_PASS_PATTERN);
        if (!matcher.matches()) return null;

        String destination = matcher.group(1);
        int price = Integer.parseInt(matcher.group(2));

        String shorthand = destination.substring(0, 2);
        return new SeaskipperDestinationItem(destination, price, shorthand);
    }
}
