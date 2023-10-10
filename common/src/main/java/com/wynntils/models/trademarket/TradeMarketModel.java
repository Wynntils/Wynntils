/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.trademarket;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.screens.trademarket.TradeMarketSearchResultHolder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TradeMarketModel extends Model {
    private static final Pattern[] ITEM_NAME_PATTERNS = {
        // Item on the create buy order menu or create sell offer menu
        Pattern.compile("^§6(?:Buying|Selling) [^ ]+ (.+?)(?:§6)? for .+ Each$"),
        // Items on the trade overview menu
        Pattern.compile("^§6(?:Buying|Selling) [^ ]+ (.+)$"),
        // Item on the view existing sell offer menu (on the right side)
        Pattern.compile("^§7§l[^ ]+x (.+)$")
    };

    private static final Pattern TRADE_MARKET_FILTER_SCREEN_TITLE_PATTERN =
            Pattern.compile("\\[Pg\\. \\d+\\] Filter Items");

    @Persisted
    private final Storage<Map<Integer, String>> presetFilters = new Storage<>(new TreeMap<>());

    private String lastSearchFilter = "";

    public TradeMarketModel() {
        super(List.of());

        Handlers.Item.addSimplifiablePatterns(ITEM_NAME_PATTERNS);
        Handlers.WrappedScreen.registerWrappedScreen(new TradeMarketSearchResultHolder());
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

    public Optional<String> getPresetFilter(int presetId) {
        return Optional.ofNullable(presetFilters.get().get(presetId));
    }

    public void setPresetFilter(int presetId, String filter) {
        presetFilters.get().put(presetId, filter);
        presetFilters.touched();
    }
}
