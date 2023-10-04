/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wrappedscreens.activities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.wrappedscreen.WrappedScreen;
import com.wynntils.handlers.wrappedscreen.WrappedScreenHolder;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.items.items.gui.ActivityItem;
import com.wynntils.screens.base.widgets.SortableActivityScreen;
import com.wynntils.utils.wynn.ContainerUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * A common superclass for all activity screen related holders.
 * Implementation that works the same way for all activity types should be put here.
 */
public abstract class AbstractActivityHolder<T extends Screen & WrappedScreen & SortableActivityScreen<I>, I>
        extends WrappedScreenHolder<T> {
    // Same as ActivityModel, but we can't reference it because it would change the class loading order
    private static final Pattern CONTENT_BOOK_TITLE_PATTERN = Pattern.compile("^§f\uE000\uE072$");

    // Constants
    protected static final int CONTENT_ITEMS_PER_PAGE = 45;
    protected static final int PREVIOUS_PAGE_SLOT = 65;
    protected static final int NEXT_PAGE_SLOT = 69;
    protected static final int FILTER_SLOT = 66;
    protected static final int SORT_SLOT = 67;

    // Screen
    protected T wrappedScreen;
    protected WrappedScreenInfo wrappedScreenInfo;

    // State
    protected int currentPage = 0;
    protected Map<Integer, Int2ObjectOpenHashMap<I>> infoMap = new TreeMap<>();
    protected int emptySlotsOnPage = 0;

    protected abstract I itemToInfo(ActivityInfo activityInfo);

    @Override
    protected Pattern getReplacedScreenTitlePattern() {
        return CONTENT_BOOK_TITLE_PATTERN;
    }

    @Override
    protected void setWrappedScreen(T wrappedScreen) {
        this.wrappedScreen = wrappedScreen;
        wrappedScreenInfo = wrappedScreen.getWrappedScreenInfo();
    }

    @Override
    protected void reset() {
        currentPage = 0;
        infoMap = new TreeMap<>();
        emptySlotsOnPage = 0;
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getContainerId() != wrappedScreenInfo.containerId()) return;

        int slot = event.getSlot();

        // We only care about the activity items
        if (slot >= CONTENT_ITEMS_PER_PAGE) return;

        Int2ObjectOpenHashMap<I> currentPageMap =
                infoMap.computeIfAbsent(this.currentPage, k -> new Int2ObjectOpenHashMap<>());

        ItemStack itemStack = event.getItemStack();

        if (itemStack.isEmpty()) {
            emptySlotsOnPage++;
        } else {
            Optional<ActivityItem> activityItemOpt = Models.Item.asWynnItem(itemStack, ActivityItem.class);
            if (activityItemOpt.isEmpty()) {
                WynntilsMod.warn("Item in slot " + slot + " is not an activity item");
                return;
            }

            ActivityItem activityItem = activityItemOpt.get();
            I info = itemToInfo(activityItem.getActivityInfo());

            currentPageMap.put(slot, info);
        }

        if (currentPageMap.size() + emptySlotsOnPage == CONTENT_ITEMS_PER_PAGE) {
            wrappedScreen.activitiesChanged(infoMap.values().stream()
                    .flatMap(map -> map.values().stream())
                    .toList());
        }
    }

    public void tryLoadNextPage() {
        // If the current page is not full, we don't need to load the next page
        Int2ObjectOpenHashMap<I> currentPageMap = infoMap.getOrDefault(currentPage, new Int2ObjectOpenHashMap<>());
        if (currentPageMap.size() != CONTENT_ITEMS_PER_PAGE) {
            return;
        }

        // We did not have a full page, there is no next page
        if (emptySlotsOnPage != 0) return;

        // FIXME: Check for "next" item

        this.currentPage++;
        ContainerUtils.clickOnSlot(
                NEXT_PAGE_SLOT,
                wrappedScreenInfo.containerId(),
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                wrappedScreenInfo.containerMenu().getItems());
    }
}
