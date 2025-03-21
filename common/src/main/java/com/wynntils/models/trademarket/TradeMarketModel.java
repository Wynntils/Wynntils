/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.trademarket;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.containers.trademarket.TradeMarketBuyContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketFiltersContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketOrderContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketSellContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketTradesContainer;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.trademarket.type.TradeMarketPriceInfo;
import com.wynntils.models.trademarket.type.TradeMarketState;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.screens.trademarket.TradeMarketSearchResultHolder;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public class TradeMarketModel extends Model {
    private static final Pattern[] ITEM_NAME_PATTERNS = {
        // Item on the create buy order menu or create sell offer menu
        Pattern.compile("^§6(?:Buying|Selling) [^ ]+ (.+?)(?:§6)? for .+ Each$"),
        // Items on the trade overview menu
        Pattern.compile("^§6(?:Buying|Selling) [^ ]+ (.+)$"),
        // Item on the view existing sell offer menu (on the right side)
        Pattern.compile("^§7§l[^ ]+x (.+)$")
    };

    private static final Pattern SEARCH_INPUT_PATTERN =
            Pattern.compile("^§5(\uE00A\uE002|\uE001) Type the item name or type 'cancel' to cancel:$");
    private static final Pattern AMOUNT_INPUT_PATTERN = Pattern.compile(
            "^§5(\uE00A\uE002|\uE001) Type the amount you wish to (buy|sell) or type 'cancel' to cancel:$");
    private static final Pattern PRICE_INPUT_PATTERN = Pattern.compile(
            "^§5(\uE00A\uE002|\uE001) Type the price in emeralds or formatted \\(e\\.g '10eb', '10stx 5eb'\\) or type 'cancel' to cancel:$");
    private static final Pattern CANCELLED_PATTERN =
            Pattern.compile("^§4(\uE008\uE002|\uE001) You moved and your chat input was canceled.$");

    // Price Parsing
    private static final int TRADE_MARKET_PRICE_LINE = 1;
    private static final Pattern PRICE_STR = Pattern.compile("§6Price:");

    public static final int TM_SELL_PRICE_SLOT = 28;
    private static final Pattern TM_SELL_PRICE_PATTERN = Pattern.compile("- §7Per Unit:§f (\\d{1,3}(?:,\\d{3})*)");
    private static final int TM_PRICE_CHECK_SLOT = 51;
    private static final Pattern TM_PRICE_CHECK_PATTERN =
            Pattern.compile("§7Cheapest Sell Offer: §f(\\d{1,3}(?:,\\d{3})*)");

    // Test in TradeMarketModel_PRICE_PATTERN
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "§[67] - (?:§f(?<amount>[\\d,]+) §7x )?§(?:(?:(?:c✖|a✔) §f)|f§m|f)(?<price>[\\d,]+)§7(?:§m)?²(?:§b ✮ (?<silverbullPrice>[\\d,]+)§3²)?(?: .+)?");

    @Persisted
    private final Storage<Map<Integer, String>> presetFilters = new Storage<>(new TreeMap<>());

    private String lastSearchFilter = "";

    // Trade Market State
    private static final ContainerBounds FILTER_SLOTS = new ContainerBounds(0, 0, 4, 2);
    private static final String NAME_FILTER = "Name Contains";
    private boolean filtersActive = false;
    private boolean nameFiltersActive = false;
    private TradeMarketState tradeMarketState = TradeMarketState.NOT_ACTIVE;

    public TradeMarketModel() {
        super(List.of());

        Handlers.Item.addSimplifiablePatterns(ITEM_NAME_PATTERNS);
        Handlers.WrappedScreen.registerWrappedScreen(new TradeMarketSearchResultHolder());
    }

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Post event) {
        // If we open a new filter screen, reset the last search filter
        if (Models.Container.getCurrentContainer() instanceof TradeMarketFiltersContainer) {
            lastSearchFilter = "";
        }

        if (Models.Container.getCurrentContainer() != null) {
            updateStateFromContainer();
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent event) {
        if (inChatInput()) return;

        tradeMarketState = TradeMarketState.NOT_ACTIVE;
        filtersActive = false;
        nameFiltersActive = false;
    }

    @SubscribeEvent
    public void onContainerItemsSet(ContainerSetContentEvent.Pre event) {
        if (tradeMarketState != TradeMarketState.FILTERS_PAGE) return;

        nameFiltersActive = false;
        filtersActive = false;

        // The default and filtered results both use the same container name so the only way we can tell if the results
        // are filtered is if there is a filter in this slot or if a search query has been given
        FILTER_SLOTS.getSlots().forEach(slot -> {
            if (event.getItems().get(slot).getHoverName().getString().equals(NAME_FILTER)) {
                nameFiltersActive = true;
            } else {
                filtersActive = filtersActive || event.getItems().get(slot).getItem() != Items.AIR;
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChatMessageReceive(ChatMessageReceivedEvent event) {
        StyledText styledText =
                StyledTextUtils.unwrap(event.getOriginalStyledText()).stripAlignment();

        if (styledText.matches(SEARCH_INPUT_PATTERN)) {
            tradeMarketState = TradeMarketState.SEARCH_CHAT_INPUT;
        } else if (styledText.matches(AMOUNT_INPUT_PATTERN)) {
            tradeMarketState = TradeMarketState.AMOUNT_CHAT_INPUT;
        } else if (styledText.matches(PRICE_INPUT_PATTERN)) {
            tradeMarketState = TradeMarketState.PRICE_CHAT_INPUT;
        } else if (styledText.matches(CANCELLED_PATTERN)) {
            tradeMarketState = TradeMarketState.NOT_ACTIVE;
        }
    }

    @SubscribeEvent
    public void onClientChat(ChatSentEvent event) {
        if (tradeMarketState != TradeMarketState.SEARCH_CHAT_INPUT) return;
        if (!nameFiltersActive && event.getMessage().equalsIgnoreCase("cancel")) return;

        if (!event.getMessage().isEmpty()) {
            nameFiltersActive = true;
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        tradeMarketState = TradeMarketState.NOT_ACTIVE;
        nameFiltersActive = false;
        filtersActive = false;
    }

    // FIXME: The screen title can no longer be used to determine the difference between filtered and non-filtered
    // results, fix or remove when fixing the custom trade market feature
    public boolean isFilterScreen(Component component) {
        return false;
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

    public TradeMarketPriceInfo calculateItemPriceInfo(ItemStack itemStack) {
        List<StyledText> loreLines = LoreUtils.getLore(itemStack);

        StyledText priceLine = loreLines.get(TRADE_MARKET_PRICE_LINE);

        if (priceLine == null || !priceLine.matches(PRICE_STR)) {
            WynntilsMod.warn("Trade Market item had an unexpected price line: " + priceLine);
            return TradeMarketPriceInfo.EMPTY;
        }

        StyledText priceValueLine = loreLines.get(TRADE_MARKET_PRICE_LINE + 1);

        Matcher matcher = priceValueLine.getMatcher(PRICE_PATTERN);
        if (!matcher.matches()) {
            WynntilsMod.warn("Trade Market item had an unexpected price value line: " + priceValueLine);
            return TradeMarketPriceInfo.EMPTY;
        }

        int price = Integer.parseInt(matcher.group("price").replace(",", ""));

        String silverbullPriceStr = matcher.group("silverbullPrice");
        int silverbullPrice =
                silverbullPriceStr == null ? price : Integer.parseInt(silverbullPriceStr.replace(",", ""));

        String amountStr = matcher.group("amount");
        int amount = amountStr == null ? 1 : Integer.parseInt(amountStr.replace(",", ""));

        return new TradeMarketPriceInfo(price, silverbullPrice, amount);
    }

    /**
     * @return The unit price of the item currently being sold in the sell screen.
     */
    public int getUnitPrice() {
        if (!(McUtils.mc().screen instanceof ContainerScreen cs)) return -1;
        if (!(Models.Container.getCurrentContainer() instanceof TradeMarketSellContainer)) return -1;

        ItemStack priceCheckItem = cs.getMenu().getItems().get(TM_SELL_PRICE_SLOT);
        if (priceCheckItem.isEmpty()) return -1;

        String lore = LoreUtils.getStringLore(priceCheckItem).getString();
        Matcher priceCheckMatcher = TM_SELL_PRICE_PATTERN.matcher(lore);
        if (priceCheckMatcher.find()) {
            String priceCheckString = priceCheckMatcher.group(1);
            return Integer.parseInt(priceCheckString.replace(",", ""));
        }

        return -1;
    }

    /**
     * @return The TM's lowest price for the current item from the price check slot.
     */
    public int getLowestPrice() {
        if (!(McUtils.mc().screen instanceof ContainerScreen cs)) return -1;
        if (!(Models.Container.getCurrentContainer() instanceof TradeMarketSellContainer)) return -1;

        ItemStack priceCheckItem = cs.getMenu().getItems().get(TM_PRICE_CHECK_SLOT);
        if (priceCheckItem.isEmpty()) return -1;

        String lore = LoreUtils.getStringLore(priceCheckItem).getString();
        Matcher priceCheckMatcher = TM_PRICE_CHECK_PATTERN.matcher(lore);
        if (priceCheckMatcher.find()) {
            String priceCheckString = priceCheckMatcher.group(1);
            return Integer.parseInt(priceCheckString.replace(",", ""));
        }

        return -1;
    }

    private void updateStateFromContainer() {
        Container currentContainer = Models.Container.getCurrentContainer();

        if (currentContainer instanceof TradeMarketContainer) {
            tradeMarketState = nameFiltersActive || filtersActive
                    ? TradeMarketState.FILTERED_RESULTS
                    : TradeMarketState.DEFAULT_RESULTS;
        } else if (currentContainer instanceof TradeMarketFiltersContainer) {
            tradeMarketState = TradeMarketState.FILTERS_PAGE;
        } else if (currentContainer instanceof TradeMarketSellContainer) {
            tradeMarketState = TradeMarketState.SELLING;
        } else if (currentContainer instanceof TradeMarketBuyContainer) {
            tradeMarketState = TradeMarketState.BUYING;
        } else if (currentContainer instanceof TradeMarketTradesContainer) {
            tradeMarketState = TradeMarketState.VIEWING_TRADES;
        } else if (currentContainer instanceof TradeMarketOrderContainer) {
            tradeMarketState = TradeMarketState.VIEWING_ORDER;
        }
    }

    public boolean inChatInput() {
        return tradeMarketState == TradeMarketState.SEARCH_CHAT_INPUT
                || tradeMarketState == TradeMarketState.AMOUNT_CHAT_INPUT
                || tradeMarketState == TradeMarketState.PRICE_CHAT_INPUT;
    }

    public boolean inTradeMarket() {
        return tradeMarketState != TradeMarketState.NOT_ACTIVE;
    }

    public TradeMarketState getTradeMarketState() {
        return tradeMarketState;
    }
}
