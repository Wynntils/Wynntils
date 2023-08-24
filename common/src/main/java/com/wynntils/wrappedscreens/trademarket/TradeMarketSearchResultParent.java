/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wrappedscreens.trademarket;

import com.wynntils.core.components.Services;
import com.wynntils.handlers.wrappedscreen.WrappedScreenParent;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class TradeMarketSearchResultParent extends WrappedScreenParent<TradeMarketSearchResultScreen> {
    // Patterns
    private static final Pattern TITLE_PATTERN = Pattern.compile("Search Results");

    // Constants
    private static final int EXPECTED_ITEMS_PER_PAGE = 42;
    private static final int LAST_ITEM_SLOT = 54;
    private static final int PAGE_BATCH_SIZE = 10;

    // Slots
    private static final int PREVIOUS_PAGE_SLOT = 26;
    private static final int NEXT_PAGE_SLOT = 35;

    private TradeMarketSearchResultScreen wrappedScreen;

    private int requestedPage = -1;
    private int currentPage = 0;

    private boolean expectingItems = false;
    private int lastPageNumber = -1;

    private Map<Integer, Int2ObjectOpenHashMap<ItemStack>> itemMap = new HashMap<>();
    private int emptyItemCount = 0;

    private Map<Integer, ItemStack> displayedItems = new HashMap<>();

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        if (!expectingItems || requestedPage == -1) return;

        WrappedScreenInfo wrappedScreenInfo = wrappedScreen.getWrappedScreenInfo();
        if (event.getContainerId() != wrappedScreenInfo.containerId()) return;

        // We only use set slot events to get the items,
        // but we don't want the items to be set on our custom screen
        event.setCanceled(true);

        // Reset the empty item count,
        // set content packets mean we are on a new page
        emptyItemCount = 0;
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        if (!expectingItems || requestedPage == -1) return;

        WrappedScreenInfo wrappedScreenInfo = wrappedScreen.getWrappedScreenInfo();
        if (event.getContainerId() != wrappedScreenInfo.containerId()) return;

        int slot = event.getSlot();

        // We only care about the slots that are in the "search results" area
        if (slot % 9 >= 7 || slot >= LAST_ITEM_SLOT) return;

        // We don't want the items to be set on our custom screen
        event.setCanceled(true);

        ItemStack itemStack = event.getItemStack();

        boolean emptyItem = isEmptyItem(itemStack);
        if (!emptyItem) {
            itemMap.computeIfAbsent(currentPage, k -> new Int2ObjectOpenHashMap<>())
                    .put(slot, itemStack);
        } else {
            emptyItemCount++;
        }

        checkStateAfterItemSet();
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

        // Preload the first batch of pages
        loadPage(PAGE_BATCH_SIZE);
    }

    @Override
    protected void reset() {
        currentPage = 0;
        itemMap.clear();

        this.wrappedScreen = null;
    }

    private void checkStateAfterItemSet() {
        wrappedScreen.setCurrentState(Component.literal("Loading page " + (currentPage + 1) + "..."));

        // If we have the item count we expect on the page, we can load the next page
        // If we have found an empty item, we count them and check if we have the expected amount
        Int2ObjectOpenHashMap<ItemStack> currentPage =
                itemMap.computeIfAbsent(this.currentPage, k -> new Int2ObjectOpenHashMap<>());
        if (currentPage.size() + emptyItemCount == EXPECTED_ITEMS_PER_PAGE) {
            if (this.currentPage == 0) {
                displayedItems = currentPage;
            }

            // If we have air items on the page, we reached the end
            if (emptyItemCount != 0) {
                expectingItems = false;
                requestedPage = -1;
                lastPageNumber = this.currentPage;
                wrappedScreen.setCurrentState(Component.literal("All pages loaded"));
                return;
            }

            if (this.currentPage == requestedPage) {
                expectingItems = false;
                requestedPage = -1;
                wrappedScreen.setCurrentState(Component.literal((itemMap.size()) + " pages loaded"));
                return;
            }

            this.currentPage++;
            emptyItemCount = 0;

            int clickSlot;
            if (this.currentPage < requestedPage) {
                clickSlot = NEXT_PAGE_SLOT;
            } else {
                clickSlot = PREVIOUS_PAGE_SLOT;
            }

            WrappedScreenInfo wrappedScreenInfo = wrappedScreen.getWrappedScreenInfo();
            ContainerUtils.clickOnSlot(
                    clickSlot,
                    wrappedScreenInfo.containerId(),
                    GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    wrappedScreenInfo.containerMenu().getItems());
        }
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

    private void loadPage(int page) {
        expectingItems = true;

        // 0 based indexing
        requestedPage = page - 1;
    }

    private boolean isEmptyItem(ItemStack itemStack) {
        ListTag loreTag = LoreUtils.getLoreTag(itemStack);
        return itemStack.getItem() == Items.SNOW && (loreTag == null || loreTag.isEmpty());
    }
}
