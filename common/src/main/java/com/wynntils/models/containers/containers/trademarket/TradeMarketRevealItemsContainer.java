/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.trademarket;

import com.wynntils.models.containers.Container;
import java.util.function.Predicate;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TradeMarketRevealItemsContainer extends Container {
    public TradeMarketRevealItemsContainer() {
        super((Predicate<Screen>) screen -> {
            Component title = screen.getTitle();
            if (title == null) return false;
            String str = title.getString();
            if (str == null) return false;
            return str.trim().equals("What would you like to reveal?");
        });
    }
}
