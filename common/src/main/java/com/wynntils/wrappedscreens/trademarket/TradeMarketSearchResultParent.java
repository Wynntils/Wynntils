/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wrappedscreens.trademarket;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.handlers.wrappedscreen.WrappedScreenParent;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.wynn.ContainerUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class TradeMarketSearchResultParent extends WrappedScreenParent<TradeMarketSearchResultScreen> {
    // Patterns
    private static final Pattern TITLE_PATTERN = Pattern.compile("Search Results");

    // Constants
    private static final int EXPECTED_ITEMS_PER_PAGE = 42;
    private static final int PAGE_BATCH_SIZE = 10;

    // Slots
    private static final int NEXT_PAGE_SLOT = 35;

    private TradeMarketSearchResultScreen wrappedScreen;

    private int currentPage = 0;
    private Map<Integer, Int2ObjectOpenHashMap<ItemStack>> itemMap = new HashMap<>();

    private Map<Integer, ItemStack> displayedItems = new HashMap<>();

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        WrappedScreenInfo wrappedScreenInfo = wrappedScreen.getWrappedScreenInfo();
        if (event.getContainerId() != wrappedScreenInfo.containerId()) return;

        // We only use set slot events to get the items,
        // but we don't want the items to be set on our custom screen
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        WrappedScreenInfo wrappedScreenInfo = wrappedScreen.getWrappedScreenInfo();
        if (event.getContainerId() != wrappedScreenInfo.containerId()) return;

        int slot = event.getSlot();

        // We only care about the slots that are in the "search results" area
        if (slot % 9 >= 7) return;

        // We don't want the items to be set on our custom screen
        event.setCanceled(true);

        itemMap.computeIfAbsent(currentPage, k -> new Int2ObjectOpenHashMap<>()).put(slot, event.getItemStack());

        if (itemMap.get(currentPage).size() == EXPECTED_ITEMS_PER_PAGE) {
            WynntilsMod.info("Page " + currentPage + " is full, moving to page " + (currentPage + 1) + "...");

            if (currentPage == 0) {
                displayedItems = itemMap.get(currentPage);
            }

            currentPage++;

            if (currentPage >= PAGE_BATCH_SIZE) return;

            ContainerUtils.clickOnSlot(
                    NEXT_PAGE_SLOT,
                    wrappedScreenInfo.containerId(),
                    GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    wrappedScreenInfo.containerMenu().getItems());
        }
    }

    @Override
    protected Pattern getReplacedScreenTitlePattern() {
        return TITLE_PATTERN;
    }

    @Override
    protected TradeMarketSearchResultScreen createWrappedScreen(WrappedScreenInfo wrappedScreenInfo) {
        return new TradeMarketSearchResultScreen(wrappedScreenInfo, this);
    }

    @Override
    protected void setWrappedScreen(TradeMarketSearchResultScreen wrappedScreen) {
        this.wrappedScreen = wrappedScreen;
    }

    @Override
    protected void reset() {
        currentPage = 0;
        itemMap.clear();

        this.wrappedScreen = null;
    }

    public void updateItems(ItemSearchQuery searchQuery) {
        displayedItems.clear();

        List<ItemStack> items = itemMap.values().stream()
                .map(map -> map.values().toArray(new ItemStack[0]))
                .flatMap(Stream::of)
                .toList();

        List<ItemStack> matchingItems = items.stream()
                .filter(itemStack -> Services.ItemFilter.matches(searchQuery, itemStack))
                .toList();
        for (int i = 0; i < matchingItems.size(); i++) {
            int slot = i / 7 * 9 + i % 7;

            displayedItems.put(slot, matchingItems.get(i));
        }

        for (int i = matchingItems.size(); i < EXPECTED_ITEMS_PER_PAGE; i++) {
            int slot = i / 7 * 9 + i % 7;

            displayedItems.put(slot, ItemStack.EMPTY);
        }
    }
}
