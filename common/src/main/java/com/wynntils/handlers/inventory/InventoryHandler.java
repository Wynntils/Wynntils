/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.inventory.ImprovedInventorySyncFeature;
import com.wynntils.handlers.inventory.event.InventoryInteractionEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Confidence;
import com.wynntils.utils.wynn.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class InventoryHandler extends Handler {
    private static final long SYNC_WINDOW_MS = 500L;

    private RunningInteraction running;
    private final List<PendingInteractionEntry> pending = new LinkedList<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSlotClick(ContainerClickEvent event) {
        if (running != null && running.handle(event)) return;

        AbstractContainerMenu menu = event.getContainerMenu();
        ItemStack heldStack = menu.getCarried();
        int slotNum = event.getSlotNum();
        switch (event.getClickType()) {
            case PICKUP -> {
                if (slotNum < 0) { // Slot -999 -> tossed an item outside the GUI
                    if (!heldStack.isEmpty()) {
                        boolean throwAll = event.getMouseButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
                        if (throwAll || heldStack.getCount() == 1) {
                            if (isImprovedSyncEnabled()) {
                                forceSyncLater(menu);
                            } else {
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
                        boolean placeAll = event.getMouseButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
                        if (placeAll || heldStack.getCount() == 1) {
                            if (isImprovedSyncEnabled()) {
                                forceSyncLater(menu);
                            } else {
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
                        boolean throwAll = event.getMouseButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
                        if (throwAll || heldStack.getCount() == 1) {
                            if (isImprovedSyncEnabled()) {
                                forceSyncLater(menu);
                            } else {
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
                        if (isImprovedSyncEnabled()) {
                            forceSyncLater(menu);
                            expect(new PendingInteraction.ThrowFromSlot(menu, slotNum, slotStack.copy()));
                        } else {
                            ItemStack thrown =
                                    event.getMouseButton() == 0 ? slotStack.copyWithCount(1) : slotStack.copy();
                            WynntilsMod.postEvent(new InventoryInteractionEvent(
                                    menu,
                                    new InventoryInteraction.ThrowFromSlot(slotNum, thrown),
                                    Confidence.UNCERTAIN));
                        }
                    }
                }
            }
            case QUICK_CRAFT -> {
                if (!heldStack.isEmpty()
                        && AbstractContainerMenu.getQuickcraftHeader(event.getMouseButton())
                                == AbstractContainerMenu.QUICKCRAFT_HEADER_START) {
                    running = new RunningInteraction(
                            menu,
                            AbstractContainerMenu.getQuickcraftType(event.getMouseButton())
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

    public void forceSync(AbstractContainerMenu menu) {
        // Method 1: click a non-slot placeholder item in a bank-like inventory
        if (menu.slots.size() > 36 && menu instanceof ChestMenu) {
            for (int slotNum = menu.slots.size() - 37; slotNum >= 0; slotNum--) {
                if (ItemUtils.isNonSlotPlaceholder(menu.getSlot(slotNum).getItem())) {
                    // Click the placeholder item, which should be no-op, but which will prompt an update packet
                    McUtils.sendPacket(new ServerboundContainerClickPacket(
                            menu.containerId,
                            menu.getStateId(),
                            slotNum,
                            0,
                            ClickType.PICKUP,
                            ItemStack.EMPTY,
                            Int2ObjectMaps.emptyMap()));
                    return;
                }
            }
        }

        // Method 2: swap the content book into an empty slot
        ItemStack contentBookStack = McUtils.player().getInventory().getItem(8);
        if (!contentBookStack.isEmpty()) {
            for (int slotNum = 0; slotNum <= menu.slots.size(); slotNum++) {
                Slot slot = menu.slots.get(slotNum);
                if (slot.getItem().isEmpty() && slot.mayPlace(contentBookStack)) {
                    // Swap the book into the slot, which should be no-op, but which will prompt an update packet
                    McUtils.sendPacket(new ServerboundContainerClickPacket(
                            menu.containerId,
                            menu.getStateId(),
                            slotNum,
                            8,
                            ClickType.SWAP,
                            ItemStack.EMPTY,
                            Int2ObjectMaps.emptyMap()));
                    return;
                }
            }
        }

        // Method 3: swap the ingredient pouch into the content book
        // This is the method of last resort because in certain inventories with filtered interactions (e.g. housing
        // inventories or blacksmiths), swapping the pouch will prompt an annoying error message in the chat
        int pouchSlot = menu instanceof InventoryMenu ? 13 : (menu.slots.size() - 32);
        // FIXME Reversed dependency of handler on model
        if (Models.Item.asWynnItem(menu.getSlot(pouchSlot).getItem(), IngredientPouchItem.class)
                .isPresent()) {
            // Swap the pouch into the content book slot, which should be no-op, but which will prompt an update packet
            McUtils.sendPacket(new ServerboundContainerClickPacket(
                    menu.containerId,
                    menu.getStateId(),
                    pouchSlot,
                    8,
                    ClickType.SWAP,
                    ItemStack.EMPTY,
                    Int2ObjectMaps.emptyMap()));
            return;
        }
    }

    private void forceSyncLater(AbstractContainerMenu menu) {
        Managers.TickScheduler.scheduleNextTick(() -> forceSync(menu));
    }

    private void expect(PendingInteraction interaction) {
        pending.add(new PendingInteractionEntry(interaction));
    }

    private void updatePending(Predicate<PendingInteraction> consumer) {
        long now = System.currentTimeMillis();
        pending.removeIf(entry -> now >= entry.expiryTime || !consumer.test(entry.interaction));
    }

    private static boolean isImprovedSyncEnabled() {
        // FIXME Reversed dependency of handler on feature
        return Managers.Feature.getFeatureInstance(ImprovedInventorySyncFeature.class)
                .isEnabled();
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

            switch (AbstractContainerMenu.getQuickcraftHeader(event.getMouseButton())) {
                case AbstractContainerMenu.QUICKCRAFT_HEADER_CONTINUE -> slots.add(event.getSlotNum());
                case AbstractContainerMenu.QUICKCRAFT_HEADER_END -> {
                    if (isImprovedSyncEnabled()) {
                        forceSyncLater(menu);
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
}
