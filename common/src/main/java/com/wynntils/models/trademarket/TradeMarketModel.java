/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.trademarket;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ScreenOpenedEvent;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TradeMarketModel extends Model {
    private static final Pattern TRADE_MARKET_FILTER_SCREEN_TITLE_PATTERN =
            Pattern.compile("\\[Pg. \\d+\\] Filter Items");

    private String lastSearchFilter = "";

    public TradeMarketModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Post event) {
        if (!isFilterScreen(event.getScreen().getTitle())) return;

        // If we open a new filter screen, reset the last search filter
        lastSearchFilter = "";
    }

    public boolean isFilterScreen(Component component) {
        return StyledText.fromComponent(component).matches(TRADE_MARKET_FILTER_SCREEN_TITLE_PATTERN);
    }

    public String getLastSearchFilter() {
        return lastSearchFilter;
    }

    public void setLastSearchFilter(String lastSearchFilter) {
        this.lastSearchFilter = lastSearchFilter;
    }
}
