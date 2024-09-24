/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Managers;
import com.wynntils.handlers.inventory.event.DesyncableContainerClickEvent;
import com.wynntils.handlers.inventory.event.InventoryInteractionEvent;
import com.wynntils.handlers.inventory.resync.InventoryResyncStrategy;
import com.wynntils.handlers.inventory.resync.InventoryResynchronizer;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.type.Confidence;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class InventoryHandler extends Handler {
    private static final long SYNC_WINDOW_MS = 500L;

    private RunningInteraction running;
    private final List<PendingInteractionEntry> pending = new LinkedList<>();

    private ResyncState resyncState = null;

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSlotClick(ContainerClickEvent event) {
        if (running != null && running.handle(event)) return;

        AbstractContainerMenu menu = event.getContainerMenu();
        ItemStack heldStack = menu.getCarried();
        int slotNum = event.getSlotNum();
        int mouseButton = event.getMouseButton();
        switch (event.getClickType()) {
            case PICKUP -> {
                if (slotNum < 0) { // Slot -999 -> tossed an item outside the GUI
                    if (!heldStack.isEmpty()) {
                        boolean throwAll = mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT;
                        if (throwAll || heldStack.getCount() == 1) {
                            if (!requestResync(menu, slotNum, ClickType.PICKUP, mouseButton)) {
                                ItemStack thrown = throwAll ? heldStack.copy() : heldStack.copyWithCount(1);
                                WynntilsMod.postEvent(new InventoryInteractionEvent(
                                        menu, new InventoryInteraction.ThrowFromHeld(thrown), Confidence.UNCERTAIN));
                                return;
                            }
                        }
                        expect(new PendingInteraction.ThrowFromHeld(menu, heldStack.copy()));
                    }
                } else {
                    ItemStack slotStack = menu.getSlot(slotNum).getItem();
                    if (heldStack.isEmpty()) {
                        if (!slotStack.isEmpty()) {
                            expect(new PendingInteraction.PickUp(menu, slotNum, slotStack.copy()));
                        }
                    } else if (slotStack.isEmpty()) {
                        boolean placeAll = mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT;
                        if (placeAll || heldStack.getCount() == 1) {
                            if (!requestResync(menu, slotNum, ClickType.PICKUP, mouseButton)) {
                                ItemStack placed = placeAll ? heldStack.copy() : heldStack.copyWithCount(1);
                                WynntilsMod.postEvent(new InventoryInteractionEvent(
                                        menu, new InventoryInteraction.Place(slotNum, placed), Confidence.UNCERTAIN));
                                return;
                            }
                        }
                        expect(new PendingInteraction.PlaceOrSwap(menu, slotNum, heldStack.copy(), ItemStack.EMPTY));
                    } else {
                        expect(new PendingInteraction.PlaceOrSwap(menu, slotNum, heldStack.copy(), slotStack.copy()));
                    }
                }
            }
            case QUICK_MOVE -> {
                ItemStack slotStack = menu.getSlot(slotNum).getItem();
                if (!slotStack.isEmpty()) {
                    expect(new PendingInteraction.Transfer(menu, slotNum, slotStack.copy()));
                }
            }
            case THROW -> {
                if (slotNum < 0) { // Slot -999 -> tossed an item outside the GUI
                    if (!heldStack.isEmpty()) { // This probably shouldn't happen, but we'll check anyways
                        boolean throwAll = mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT;
                        if (throwAll || heldStack.getCount() == 1) {
                            if (!requestResync(menu, slotNum, ClickType.THROW, mouseButton)) {
                                ItemStack thrown = throwAll ? heldStack.copy() : heldStack.copyWithCount(1);
                                WynntilsMod.postEvent(new InventoryInteractionEvent(
                                        menu, new InventoryInteraction.ThrowFromHeld(thrown), Confidence.UNCERTAIN));
                                return;
                            }
                        }
                        expect(new PendingInteraction.ThrowFromHeld(menu, heldStack.copy()));
                    }
                } else {
                    ItemStack slotStack = menu.getSlot(slotNum).getItem();
                    if (heldStack.isEmpty() && !slotStack.isEmpty()) {
                        if (!requestResync(menu, slotNum, ClickType.THROW, mouseButton)) {
                            ItemStack thrown = mouseButton == 0 ? slotStack.copyWithCount(1) : slotStack.copy();
                            WynntilsMod.postEvent(new InventoryInteractionEvent(
                                    menu,
                                    new InventoryInteraction.ThrowFromSlot(slotNum, thrown),
                                    Confidence.UNCERTAIN));
                            return;
                        }
                        expect(new PendingInteraction.ThrowFromSlot(menu, slotNum, slotStack.copy()));
                    }
                }
            }
            case QUICK_CRAFT -> {
                if (!heldStack.isEmpty()
                        && AbstractContainerMenu.getQuickcraftHeader(mouseButton)
                                == AbstractContainerMenu.QUICKCRAFT_HEADER_START) {
                    running = new RunningInteraction(
                            menu,
                            AbstractContainerMenu.getQuickcraftType(mouseButton)
                                    == AbstractContainerMenu.QUICKCRAFT_TYPE_GREEDY,
                            heldStack.copy());
                }
            }
            case PICKUP_ALL -> {
                if (!heldStack.isEmpty()) {
                    expect(new PendingInteraction.PickUpAll(menu, heldStack.copy()));
                }
            }
                // SWAP and CLONE are not supported as inventory operations on Wynncraft
        }
    }

    @SubscribeEvent
    public void onSetContents(ContainerSetContentEvent.Pre event) {
        int containerId = event.getContainerId();
        List<ItemStack> newInv = event.getItems();
        updatePending(ixn -> ixn.onContentUpdate(containerId, newInv));
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent.Pre event) {
        int containerId = event.getContainerId();
        int slotNum = event.getSlot();
        ItemStack newStack = event.getItemStack();
        updatePending(ixn -> ixn.onSlotUpdate(containerId, slotNum, newStack));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        running = null;
        pending.clear();
    }

    private boolean requestResync(AbstractContainerMenu menu, int slotNum, ClickType clickType, int mouseButton) {
        if (resyncState != null && resyncState.menu.containerId != menu.containerId) {
            resyncState = null;
        }

        DesyncableContainerClickEvent event = new DesyncableContainerClickEvent(
                menu, slotNum, clickType, mouseButton, resyncState != null ? resyncState.resyncStrategy : null);
        WynntilsMod.postEvent(event);

        if (!event.getShouldResync()) {
            resyncState = null;
            return false;
        }
        InventoryResyncStrategy resyncStrategy = event.getResyncStrategy();
        if (resyncStrategy == null) {
            resyncState = null;
            return false;
        }

        if (resyncState == null || !resyncState.resyncStrategy.equals(resyncStrategy)) {
            resyncState = new ResyncState(menu, resyncStrategy);
        }
        Managers.TickScheduler.scheduleNextTick(resyncState::resync);
        return true;
    }

    private void expect(PendingInteraction interaction) {
        pending.add(new PendingInteractionEntry(interaction));
    }

    private void updatePending(Predicate<PendingInteraction> consumer) {
        long now = System.currentTimeMillis();
        pending.removeIf(entry -> now >= entry.expiryTime || !consumer.test(entry.interaction));
    }

    private final class RunningInteraction {
        private final AbstractContainerMenu menu;
        private final boolean single;
        private final ItemStack heldStack;
        private final IntList slots = new IntArrayList();

        private RunningInteraction(AbstractContainerMenu menu, boolean single, ItemStack heldStack) {
            this.menu = menu;
            this.single = single;
            this.heldStack = heldStack;
        }

        private boolean handle(ContainerClickEvent event) {
            if (event.getClickType() != ClickType.QUICK_CRAFT
                    || event.getContainerMenu().containerId != menu.containerId) {
                running = null;
                return false;
            }

            int mask = event.getMouseButton();
            if ((AbstractContainerMenu.getQuickcraftType(mask) == AbstractContainerMenu.QUICKCRAFT_TYPE_GREEDY)
                    != single) {
                running = null;
                return false;
            }

            switch (AbstractContainerMenu.getQuickcraftHeader(mask)) {
                case AbstractContainerMenu.QUICKCRAFT_HEADER_CONTINUE -> slots.add(event.getSlotNum());
                case AbstractContainerMenu.QUICKCRAFT_HEADER_END -> {
                    if (requestResync(menu, -999, ClickType.QUICK_CRAFT, mask)) {
                        expect(new PendingInteraction.Spread(menu, slots, heldStack, single));
                    } else {
                        WynntilsMod.postEvent(new InventoryInteractionEvent(
                                menu,
                                new InventoryInteraction.Spread(slots, heldStack.copy(), single),
                                Confidence.UNCERTAIN));
                    }
                    running = null;
                }
                default -> {
                    running = null;
                    return false;
                }
            }
            return true;
        }
    }

    private static final class PendingInteractionEntry {
        private final PendingInteraction interaction;
        private final long expiryTime;

        private PendingInteractionEntry(PendingInteraction interaction) {
            this.interaction = interaction;
            this.expiryTime = System.currentTimeMillis() + SYNC_WINDOW_MS;
        }
    }

    private static final class ResyncState {
        private final AbstractContainerMenu menu;
        private final InventoryResyncStrategy resyncStrategy;
        private InventoryResynchronizer cachedResynchronizer = null;

        private ResyncState(AbstractContainerMenu menu, InventoryResyncStrategy resyncStrategy) {
            this.menu = menu;
            this.resyncStrategy = resyncStrategy;
        }

        private void resync() {
            if (cachedResynchronizer == null || !cachedResynchronizer.isValid()) {
                cachedResynchronizer = resyncStrategy.getResynchronizer(menu);
            }
            if (cachedResynchronizer != null) {
                cachedResynchronizer.resync();
            }
        }
    }
}
