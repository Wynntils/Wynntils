/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.ServerItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class ServerAnnotator implements GuiItemAnnotator {
    private static final Pattern SERVER_ITEM_PATTERN =
            Pattern.compile("§[baec](?:§l)?World (\\d+)(§3 \\(Recommended\\))?");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(SERVER_ITEM_PATTERN);
        if (!matcher.matches()) return null;

        int serverId = Integer.parseInt(matcher.group(1));

        return new ServerItem(serverId);
    }
}
