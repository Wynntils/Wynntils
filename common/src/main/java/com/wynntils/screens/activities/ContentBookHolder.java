/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.wrappedscreen.WrappedScreenHolder;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.activities.type.ActivityTrackingState;
import com.wynntils.models.items.items.gui.ActivityItem;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ContentBookHolder extends WrappedScreenHolder<WynntilsContentBookScreen> {
    private static final Pattern TITLE_PATTERN = Pattern.compile("§f\uDAFF\uDFEE\uE004");
    private static final String TUTORIAL_ITEM_NAME = "\uDB3F\uDFFF";

    // Key represents the slot when in the SetSlotEvent. Value represents the slot in SetContentEvent
    private static final Pair<Integer, Integer> DIALOGUE_HISTORY_SLOTS = Pair.of(13, 58);
    public static final Pair<Integer, Integer> SCROLL_UP_SLOTS = Pair.of(20, 65);
    private static final Pair<Integer, Integer> FILTER_ITEM_SLOTS = Pair.of(21, 66);
    private static final Pair<Integer, Integer> SORT_ITEM_SLOTS = Pair.of(22, 67);
    private static final Pair<Integer, Integer> PROGRESS_SLOTS = Pair.of(23, 68);
    public static final Pair<Integer, Integer> SCROLL_DOWN_SLOTS = Pair.of(24, 69);

    private static final Set<Pair<Integer, Integer>> ACTION_SLOTS = Set.of(
            DIALOGUE_HISTORY_SLOTS,
            SCROLL_UP_SLOTS,
            FILTER_ITEM_SLOTS,
            SORT_ITEM_SLOTS,
            PROGRESS_SLOTS,
            SCROLL_DOWN_SLOTS);

    private Map<Integer, Pair<ItemStack, ActivityInfo>> activities = new TreeMap<>();
    private Map<Integer, ItemStack> actions = new TreeMap<>();

    public boolean inTutorial = false;
    private boolean resetNextSetContent = true;

    private WynntilsContentBookScreen wrappedScreen;

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent.Post event) {
        if (!(McUtils.screen() instanceof WynntilsContentBookScreen contentBookScreen)) return;

        if (event.getContainerId() == McUtils.inventoryMenu().containerId) {
            for (Pair<Integer, Integer> slotPair : ACTION_SLOTS) {
                if (slotPair.a() == event.getSlot()) {
                    handleActionSlot(event.getItemStack(), slotPair.b());
                    return;
                }
            }
        } else if (event.getContainerId()
                == wrappedScreen.getWrappedScreenInfo().containerId()) {
            if (event.getItemStack().isEmpty()) {
                activities.remove(event.getSlot());
                contentBookScreen.reloadContentBookWidgets(true);
                return;
            }

            boolean resetPage = handleActivityItem(event.getItemStack(), event.getSlot());
            contentBookScreen.reloadContentBookWidgets(resetPage);
        }
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Post event) {
        if (event.getContainerId() != wrappedScreen.getWrappedScreenInfo().containerId()) return;
        if (McUtils.screen() instanceof WynntilsContentBookScreen contentBookScreen) {
            activities = new TreeMap<>();

            for (int i = 0; i < event.getItems().size(); i++) {
                ItemStack item = event.getItems().get(i);

                boolean actionSlot = false;
                for (Pair<Integer, Integer> slotPair : ACTION_SLOTS) {
                    if (slotPair.b() == i) {
                        handleActionSlot(item, slotPair.b());
                        actionSlot = true;
                        break;
                    }
                }

                if (actionSlot) continue;
                if (item.isEmpty()) {
                    activities.remove(i);
                    continue;
                }

                handleActivityItem(item, i);
            }

            contentBookScreen.reloadContentBookWidgets(resetNextSetContent);
        }
    }

    public Map<Integer, Pair<ItemStack, ActivityInfo>> getActivities() {
        return Collections.unmodifiableMap(activities.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, TreeMap::new)));
    }

    public Optional<ActivityInfo> getTrackedActivityInfo() {
        return activities.values().stream()
                .filter(activityInfoPair -> activityInfoPair.b().trackingState() == ActivityTrackingState.TRACKED)
                .map(Pair::b)
                .findFirst();
    }

    public void reloadActions() {
        for (Map.Entry<Integer, ItemStack> entry : actions.entrySet()) {
            handleActionSlot(entry.getValue(), entry.getKey());
        }
    }

    public void scrollUp() {
        pressSlot(SCROLL_UP_SLOTS.b());
    }

    public void scrollDown() {
        pressSlot(SCROLL_DOWN_SLOTS.b());
    }

    public void pressSlot(int slot) {
        pressSlot(slot, GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }

    public void pressSlot(int slot, int mouseButton) {
        // Left clicking the progress or dialogue history slot will always send a SetContent event which we should
        // reset the "fake" page on, but not in these cases as the content is not changing
        resetNextSetContent = mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT
                || (slot != PROGRESS_SLOTS.b() && slot != DIALOGUE_HISTORY_SLOTS.b());

        if (KeyboardUtils.isShiftDown()) {
            ContainerUtils.shiftClickOnSlot(
                    slot,
                    wrappedScreen.getWrappedScreenInfo().containerId(),
                    mouseButton,
                    McUtils.containerMenu().getItems());
        } else {
            ContainerUtils.clickOnSlot(
                    slot,
                    wrappedScreen.getWrappedScreenInfo().containerId(),
                    mouseButton,
                    McUtils.containerMenu().getItems());
        }
    }

    private boolean handleActivityItem(ItemStack itemStack, int slot) {
        Optional<ActivityItem> activityItemOpt = Models.Item.asWynnItem(itemStack, ActivityItem.class);

        if (activityItemOpt.isPresent()) {
            boolean resetPage = activities.get(slot) == null
                    || !activities.get(slot).a().getHoverName().equals(itemStack.getHoverName());
            activities.put(slot, Pair.of(itemStack, activityItemOpt.get().getActivityInfo()));

            return resetPage;
        } else if (itemStack.getHoverName().getString().equals(TUTORIAL_ITEM_NAME)) {
            inTutorial = true;
            return false;
        } else {
            WynntilsMod.error(
                    "Item: " + itemStack.getHoverName().getString() + " was not identified as an activity item");
            activities.remove(slot);
            return true;
        }
    }

    private void handleActionSlot(ItemStack itemStack, int slot) {
        if (McUtils.screen() instanceof WynntilsContentBookScreen contentBookScreen) {
            if (slot == DIALOGUE_HISTORY_SLOTS.b()) {
                contentBookScreen.setDialogueHistoryItem(itemStack, DIALOGUE_HISTORY_SLOTS.b());
                actions.put(DIALOGUE_HISTORY_SLOTS.b(), itemStack);
            } else if (slot == SCROLL_UP_SLOTS.b()) {
                contentBookScreen.setScrollUpItem(itemStack);
                actions.put(SCROLL_UP_SLOTS.b(), itemStack);
            } else if (slot == FILTER_ITEM_SLOTS.b()) {
                contentBookScreen.setFilterItem(itemStack, FILTER_ITEM_SLOTS.b());
                actions.put(FILTER_ITEM_SLOTS.b(), itemStack);
            } else if (slot == SORT_ITEM_SLOTS.b()) {
                contentBookScreen.setSortItem(itemStack, SORT_ITEM_SLOTS.b());
                actions.put(SORT_ITEM_SLOTS.b(), itemStack);
            } else if (slot == PROGRESS_SLOTS.b()) {
                contentBookScreen.setPlayerProgressItem(itemStack, PROGRESS_SLOTS.b());
                actions.put(PROGRESS_SLOTS.b(), itemStack);
            } else if (slot == SCROLL_DOWN_SLOTS.b()) {
                contentBookScreen.setScrollDownItem(itemStack);
                actions.put(SCROLL_DOWN_SLOTS.b(), itemStack);
            }
        }
    }

    @Override
    protected Pattern getReplacedScreenTitlePattern() {
        return TITLE_PATTERN;
    }

    @Override
    protected WynntilsContentBookScreen createWrappedScreen(WrappedScreenInfo wrappedScreenInfo) {
        return new WynntilsContentBookScreen(wrappedScreenInfo, this);
    }

    @Override
    protected void setWrappedScreen(WynntilsContentBookScreen wrappedScreen) {
        this.wrappedScreen = wrappedScreen;
    }

    @Override
    protected void reset() {
        activities = new TreeMap<>();
        actions = new TreeMap<>();
        inTutorial = false;
    }
}
