/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory;

import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Managers;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                        if (event.getMouseButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT || heldStack.getCount() == 1) {
                            forceSyncLater(menu);
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
                        if (event.getMouseButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT || heldStack.getCount() == 1) {
                            forceSyncLater(menu);
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
                    forceSyncLater(menu);
                    expect(new PendingInteraction.ThrowFromSlot(menu, slotNum, slotStack.copy()));
                }
            }
            case QUICK_CRAFT -> {
                if (!heldStack.isEmpty()) {
                    running = new RunningInteraction(menu, (event.getMouseButton() & 0x4) != 0, heldStack.copy());
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
        long now = System.currentTimeMillis();
        int containerId = event.getContainerId();
        List<ItemStack> newInv = event.getItems();
        pending.removeIf(entry -> now >= entry.expiryTime || !entry.interaction.onContentUpdate(containerId, newInv));
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent.Pre event) {
        long now = System.currentTimeMillis();
        int containerId = event.getContainerId();
        int slotNum = event.getSlot();
        ItemStack newStack = event.getItemStack();
        pending.removeIf(
                entry -> now >= entry.expiryTime || !entry.interaction.onSlotUpdate(containerId, slotNum, newStack));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        running = null;
        pending.clear();
    }

    public void forceSync(AbstractContainerMenu menu) {
        McUtils.sendPacket(new ServerboundContainerClickPacket(
                menu.containerId,
                menu.getStateId(),
                Math.min(1, menu.slots.size() - 1),
                0,
                ClickType.SWAP,
                new ItemStack(Items.BARRIER, 31),
                Int2ObjectMaps.emptyMap()));
    }

    private void forceSyncLater(AbstractContainerMenu menu) {
        Managers.TickScheduler.scheduleNextTick(() -> forceSync(menu));
    }

    private void expect(PendingInteraction interaction) {
        pending.add(new PendingInteractionEntry(interaction));
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
            if (((mask & 0x4) == 0) == single) {
                running = null;
                return false;
            }

            switch (event.getMouseButton() & 0x3) {
                case 1 -> slots.add(event.getSlotNum());
                case 2 -> {
                    forceSyncLater(menu);
                    expect(new PendingInteraction.Spread(menu, slots, heldStack, single));
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
