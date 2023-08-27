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
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
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
    private int requestedItemSlot = -1;

    private int currentPage = 0;

    private PageLoadingMode pageLoadingMode = PageLoadingMode.NONE;

    private Map<Integer, Int2ObjectOpenHashMap<ItemStack>> itemMap = new HashMap<>();
    private int pageItemCount = 0;

    private List<ItemStack> filteredItems = new ArrayList<>();

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        if (pageLoadingMode == PageLoadingMode.NONE || requestedPage == -1) return;

        WrappedScreenInfo wrappedScreenInfo = wrappedScreen.getWrappedScreenInfo();
        if (event.getContainerId() != wrappedScreenInfo.containerId()) return;

        // We only use set slot events to get the items,
        // but we don't want the items to be set on our custom screen
        event.setCanceled(true);

        // Reset the empty item count,
        // set content packets mean we are on a new page
        pageItemCount = 0;
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        if (pageLoadingMode == PageLoadingMode.NONE || requestedPage == -1) return;

        WrappedScreenInfo wrappedScreenInfo = wrappedScreen.getWrappedScreenInfo();
        if (event.getContainerId() != wrappedScreenInfo.containerId()) return;

        int slot = event.getSlot();

        // We don't want the items to be set on our custom screen
        event.setCanceled(true);

        ItemStack itemStack = event.getItemStack();

        handleSetItem(slot, itemStack);
        updateDisplayItems(wrappedScreen.getSearchQuery());
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
        loadItemsUntilPage(PAGE_BATCH_SIZE);
    }

    @Override
    protected void reset() {
        currentPage = 0;
        itemMap.clear();

        this.wrappedScreen = null;
    }

    public void clickOnItem(ItemStack itemStack) {
        int page = 0;
        int slot = 0;

        for (Map.Entry<Integer, Int2ObjectOpenHashMap<ItemStack>> pageEntry : itemMap.entrySet()) {
            for (Int2ObjectMap.Entry<ItemStack> itemOnPage :
                    pageEntry.getValue().int2ObjectEntrySet()) {
                if (itemOnPage.getValue().equals(itemStack)) {
                    page = pageEntry.getKey();
                    slot = itemOnPage.getIntKey();
                    break;
                }
            }
        }

        requestedPage = page;
        requestedItemSlot = slot;
        pageLoadingMode = PageLoadingMode.SINGLE_ITEM;

        goToNextPage();
    }

    public void updateDisplayItems(ItemSearchQuery searchQuery) {
        filteredItems.clear();

        List<ItemStack> items = itemMap.values().stream()
                .map(map -> map.values().toArray(new ItemStack[0]))
                .flatMap(Stream::of)
                .toList();

        List<ItemStack> matchingItems = items.stream()
                .filter(itemStack -> Services.ItemFilter.matches(searchQuery, itemStack))
                .toList();

        filteredItems.addAll(matchingItems);
    }

    public List<ItemStack> getFilteredItems() {
        return filteredItems;
    }

    private void handleSetItem(int slot, ItemStack itemStack) {
        wrappedScreen.setCurrentState(Component.literal("Loading page " + (currentPage + 1) + "..."));

        // We only care about the slots that are in the "search results" area
        if (slot % 9 >= 7 || slot >= LAST_ITEM_SLOT) return;

        // If we have the item count we expect on the page, we can load the next page
        // If we have found an empty item, we count them and check if we have the expected amount
        Int2ObjectOpenHashMap<ItemStack> currentPage =
                itemMap.computeIfAbsent(this.currentPage, k -> new Int2ObjectOpenHashMap<>());

        boolean emptyItem = isEmptyItem(itemStack);
        if (!emptyItem) {
            ItemStack oldItem = currentPage.put(slot, itemStack);

            if (oldItem != null && !oldItem.equals(itemStack)) {
                WynntilsMod.warn("Item mismatch at slot " + slot + " on page " + this.currentPage + ". Expected: "
                        + oldItem + ", got: " + itemStack);
            }
        }

        pageItemCount++;

        if (pageItemCount == EXPECTED_ITEMS_PER_PAGE) {
            switch (pageLoadingMode) {
                case ALL_ITEMS -> {
                    pageLoadedWhileLoadingItems(currentPage);
                }
                case SINGLE_ITEM -> {
                    pageLoadedWhileSelectingItem(currentPage);
                }
            }
        }
    }

    private void pageLoadedWhileLoadingItems(Int2ObjectOpenHashMap<ItemStack> currentItems) {
        // If we have air items on the page, we reached the end
        if (currentItems.size() < EXPECTED_ITEMS_PER_PAGE) {
            pageLoadingMode = PageLoadingMode.NONE;
            requestedPage = -1;
            wrappedScreen.setCurrentState(Component.literal("All pages loaded"));
        }

        if (this.currentPage == requestedPage) {
            pageLoadingMode = PageLoadingMode.NONE;
            requestedPage = -1;
            wrappedScreen.setCurrentState(Component.literal((itemMap.size()) + " pages loaded"));
        }

        goToNextPage();
    }

    private void pageLoadedWhileSelectingItem(Int2ObjectOpenHashMap<ItemStack> currentItems) {
        if (pageItemCount != EXPECTED_ITEMS_PER_PAGE) return;

        // We are on the requested page, click on the item
        if (currentPage == requestedPage) {
            // Loaded the item, click on it
            WrappedScreenInfo wrappedScreenInfo = wrappedScreen.getWrappedScreenInfo();
            ContainerUtils.clickOnSlot(
                    requestedItemSlot,
                    wrappedScreenInfo.containerId(),
                    GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    wrappedScreenInfo.containerMenu().getItems());

            return;
        } else {
            // We are not on the requested page, load the next page
            goToNextPage();
        }
    }

    private void goToNextPage() {
        pageItemCount = 0;

        int clickSlot;
        if (this.currentPage < requestedPage) {
            clickSlot = NEXT_PAGE_SLOT;
            this.currentPage++;
        } else {
            clickSlot = PREVIOUS_PAGE_SLOT;
            this.currentPage--;
        }

        WrappedScreenInfo wrappedScreenInfo = wrappedScreen.getWrappedScreenInfo();
        ContainerUtils.clickOnSlot(
                clickSlot,
                wrappedScreenInfo.containerId(),
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                wrappedScreenInfo.containerMenu().getItems());
    }

    private void loadItemsUntilPage(int page) {
        pageLoadingMode = PageLoadingMode.ALL_ITEMS;

        // 0 based indexing
        requestedPage = page - 1;
    }

    private boolean isEmptyItem(ItemStack itemStack) {
        ListTag loreTag = LoreUtils.getLoreTag(itemStack);
        return itemStack.getItem() == Items.SNOW && (loreTag == null || loreTag.isEmpty());
    }

    public enum PageLoadingMode {
        ALL_ITEMS,
        SINGLE_ITEM,
        NONE
    }
}
