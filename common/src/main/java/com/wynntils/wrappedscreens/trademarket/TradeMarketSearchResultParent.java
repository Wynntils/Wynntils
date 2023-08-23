/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wrappedscreens.trademarket;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.handlers.wrappedscreen.WrappedScreenParent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.wynn.ContainerUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class TradeMarketSearchResultParent implements WrappedScreenParent<TradeMarketSearchResultScreen> {
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
    public void onContainerSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getContainerId() != wrappedScreen.getContainerId()) return;

        int slot = event.getSlot();

        // We only care about the slots that are in the "search results" area
        if (slot % 9 >= 7) return;

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
                    wrappedScreen.getContainerId(),
                    GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    wrappedScreen.getContainerMenu().getItems());
        }
    }

    // FIXME: Visual hack
    @SubscribeEvent
    public void onSlotRender(SlotRenderEvent.Pre event) {
        ItemStack itemStack = displayedItems.getOrDefault(event.getSlot().index, ItemStack.EMPTY);

        if (event.getSlot().index % 9 >= 7 || event.getSlot().index >= 52) {
            itemStack = event.getSlot().getItem();
        }

        event.getSlot().set(itemStack);
    }

    @Override
    public Pattern getReplacedScreenTitlePattern() {
        return TITLE_PATTERN;
    }

    @Override
    public TradeMarketSearchResultScreen createWrappedScreen(
            Screen originalScreen, AbstractContainerMenu containerMenu, int containerId) {
        return new TradeMarketSearchResultScreen(originalScreen, containerMenu, containerId);
    }

    @Override
    public void setWrappedScreen(TradeMarketSearchResultScreen wrappedScreen) {
        this.wrappedScreen = wrappedScreen;
    }

    @Override
    public void reset() {
        currentPage = 0;
        itemMap.clear();
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
