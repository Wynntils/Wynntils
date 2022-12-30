/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators.gui;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.model.item.gui.ServerItem;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;

public final class ServerAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        Matcher matcher = WynnItemMatchers.serverItemMatcher(itemStack.getHoverName());

        if (!matcher.matches()) {
            return null;
        }

        int serverId = Integer.parseInt(matcher.group(1));

        return new ServerItem(serverId);
    }
}
