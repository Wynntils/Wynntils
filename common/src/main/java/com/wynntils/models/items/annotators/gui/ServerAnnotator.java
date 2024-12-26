/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.ServerItem;
import com.wynntils.models.worlds.type.ServerRegion;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class ServerAnnotator implements GuiItemAnnotator {
    private static final Pattern SERVER_ITEM_PATTERN =
            Pattern.compile("§[baec](?:§l)?(.{2}) \\| World (\\d+)(§3 Recommended)?");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(SERVER_ITEM_PATTERN);
        if (!matcher.matches()) return null;

        ServerRegion region = ServerRegion.fromString(matcher.group(1));
        int serverId = Integer.parseInt(matcher.group(2));

        return new ServerItem(region, serverId);
    }
}
