/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory;

import static net.minecraft.world.inventory.AbstractContainerMenu.QUICKCRAFT_HEADER_CONTINUE;
import static net.minecraft.world.inventory.AbstractContainerMenu.QUICKCRAFT_HEADER_END;
import static net.minecraft.world.inventory.AbstractContainerMenu.QUICKCRAFT_HEADER_START;
import static net.minecraft.world.inventory.AbstractContainerMenu.QUICKCRAFT_TYPE_GREEDY;
import static net.minecraft.world.inventory.AbstractContainerMenu.getQuickcraftHeader;
import static net.minecraft.world.inventory.AbstractContainerMenu.getQuickcraftType;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Managers;
import com.wynntils.features.inventory.ImprovedInventorySyncFeature;
import com.wynntils.handlers.inventory.event.InventoryInteractionEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Confidence;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Inventory;
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
                ItemStack slotStack = menu.getSlot(slotNum).getItem();
                if (heldStack.isEmpty() && !slotStack.isEmpty()) {
                    if (isImprovedSyncEnabled()) {
                        forceSyncLater(menu);
                        expect(new PendingInteraction.ThrowFromSlot(menu, slotNum, slotStack.copy()));
                    } else {
                        ItemStack thrown = event.getMouseButton() == 0 ? slotStack.copyWithCount(1) : slotStack.copy();
                        WynntilsMod.postEvent(new InventoryInteractionEvent(
                                menu, new InventoryInteraction.ThrowFromSlot(slotNum, thrown), Confidence.UNCERTAIN));
                    }
                }
            }
            case QUICK_CRAFT -> {
                if (!heldStack.isEmpty() && getQuickcraftHeader(event.getMouseButton()) == QUICKCRAFT_HEADER_START) {
                    running = new RunningInteraction(
                            menu,
                            getQuickcraftType(event.getMouseButton()) == QUICKCRAFT_TYPE_GREEDY,
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
        // Find a nonempty hotbar slot
        Inventory inventory = McUtils.player().getInventory();
        for (int hotbarSlot = 8; hotbarSlot >= 0; hotbarSlot--) { // In reverse, to find the content book first
            ItemStack stack = inventory.getItem(hotbarSlot);
            if (!stack.isEmpty()) {
                // Swap the item into an inventory slot, which should be no-op, but which will prompt an update packet
                McUtils.sendPacket(new ServerboundContainerClickPacket(
                        menu.containerId,
                        menu.getStateId(),
                        menu.slots.size() > 1 ? 1 : 0, // Avoid the inventory crafting result slot, which ignores swaps
                        hotbarSlot,
                        ClickType.SWAP,
                        ItemStack.EMPTY,
                        Int2ObjectMaps.emptyMap()));
                return;
            }
        }

        // Find a nonempty inventory slot
        for (int slotNum = 9; slotNum <= menu.slots.size(); slotNum++) {
            ItemStack stack = menu.getSlot(slotNum).getItem();
            if (!stack.isEmpty()) {
                // Swap the item into hotbar slot 0, which should be no-op, but which will prompt an update packet
                McUtils.sendPacket(new ServerboundContainerClickPacket(
                        menu.containerId,
                        menu.getStateId(),
                        slotNum,
                        0,
                        ClickType.SWAP,
                        ItemStack.EMPTY,
                        Int2ObjectMaps.emptyMap()));
                return;
            }
        }
        // At this point, our view of the inventory must be completely empty; there's not much we can do
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
            if ((getQuickcraftType(mask) == QUICKCRAFT_TYPE_GREEDY) != single) {
                running = null;
                return false;
            }

            switch (getQuickcraftHeader(event.getMouseButton())) {
                case QUICKCRAFT_HEADER_CONTINUE -> slots.add(event.getSlotNum());
                case QUICKCRAFT_HEADER_END -> {
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
