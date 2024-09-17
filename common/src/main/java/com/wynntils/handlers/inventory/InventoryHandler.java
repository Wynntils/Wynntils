/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Managers;
import com.wynntils.handlers.inventory.event.InventoryInteractionEvent;
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

public class InventoryHandler extends Handler {
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
                        if (event.getMouseButton() == 0 || heldStack.getCount() == 1) {
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
                        if (event.getMouseButton() == 0 || heldStack.getCount() == 1) {
                            forceSyncLater(menu);
                        }
                        expect(new PendingInteraction.Place(menu, slotNum, heldStack.copy()));
                    } else if (areItemsSimilar(heldStack, slotStack)) {
                        expect(new PendingInteraction.Place(menu, slotNum, heldStack.copy()));
                    } else {
                        expect(new PendingInteraction.Swap(menu, slotNum, heldStack.copy(), slotStack.copy()));
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

    private static boolean areItemsSimilar(ItemStack a, ItemStack b) {
        if (a.isEmpty()) {
            return b.isEmpty();
        } else {
            return !b.isEmpty()
                    && a.getHoverName().getString().equals(b.getHoverName().getString());
        }
    }

    private static boolean areStacksSimilar(ItemStack a, ItemStack b) {
        return areItemsSimilar(a, b) && a.getCount() == b.getCount();
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

    private interface PendingInteraction {
        boolean onSlotUpdate(int containerId, int slotNum, ItemStack newStack);

        default boolean onContentUpdate(int containerId, List<ItemStack> newInv) {
            return true;
        }

        abstract class CaptureHeld implements PendingInteraction {
            @Override
            public boolean onSlotUpdate(int containerId, int slotNum, ItemStack newStack) {
                if (containerId == -1) {
                    tryDispatch(newStack);
                    return false;
                }
                return true;
            }

            protected abstract void tryDispatch(ItemStack newHeldStack);
        }

        abstract class CaptureSlot implements PendingInteraction {
            protected final AbstractContainerMenu menu;
            protected final int slotNum;

            protected CaptureSlot(AbstractContainerMenu menu, int slotNum) {
                this.menu = menu;
                this.slotNum = slotNum;
            }

            @Override
            public boolean onSlotUpdate(int containerId, int slotNum, ItemStack newStack) {
                if (containerId == menu.containerId && slotNum == this.slotNum) {
                    tryDispatch(newStack);
                    return false;
                }
                return true;
            }

            @Override
            public boolean onContentUpdate(int containerId, List<ItemStack> newInv) {
                if (containerId == menu.containerId && slotNum < newInv.size()) {
                    tryDispatch(newInv.get(slotNum));
                    return false;
                }
                return true;
            }

            protected abstract void tryDispatch(ItemStack newSlotStack);
        }

        abstract class CaptureHeldAndSlot implements PendingInteraction {
            protected final AbstractContainerMenu menu;
            protected final int slotNum;

            private ItemStack newHeldStack = null;
            private ItemStack newSlotStack = null;

            protected CaptureHeldAndSlot(AbstractContainerMenu menu, int slotNum) {
                this.menu = menu;
                this.slotNum = slotNum;
            }

            @Override
            public boolean onSlotUpdate(int containerId, int slotNum, ItemStack newStack) {
                if (containerId == -1) {
                    newHeldStack = newStack;
                    return check();
                } else if (containerId == menu.containerId && slotNum == this.slotNum) {
                    newSlotStack = newStack;
                    return check();
                }
                return true;
            }

            @Override
            public boolean onContentUpdate(int containerId, List<ItemStack> newInv) {
                if (containerId == menu.containerId && slotNum < newInv.size()) {
                    newSlotStack = newInv.get(slotNum);
                    return check();
                }
                return true;
            }

            private boolean check() {
                if (newHeldStack == null || newSlotStack == null) return true;
                tryDispatch(newHeldStack, newSlotStack);
                return false;
            }

            protected abstract void tryDispatch(ItemStack newHeldStack, ItemStack newSlotStack);
        }

        class PickUp extends CaptureHeldAndSlot {
            private final ItemStack slotStack;

            public PickUp(AbstractContainerMenu menu, int slotNum, ItemStack slotStack) {
                super(menu, slotNum);
                this.slotStack = slotStack;
            }

            @Override
            protected void tryDispatch(ItemStack newHeldStack, ItemStack newSlotStack) {
                if (!areItemsSimilar(slotStack, newHeldStack)) return;
                if (!(newSlotStack.isEmpty() || areItemsSimilar(slotStack, newSlotStack))) return;
                WynntilsMod.postEvent(new InventoryInteractionEvent(
                        menu, new InventoryInteraction.PickUp(slotNum, newHeldStack.copy(), newSlotStack.copy())));
            }
        }

        class PickUpAll extends CaptureHeld {
            private final AbstractContainerMenu menu;
            private final ItemStack heldStack;

            public PickUpAll(AbstractContainerMenu menu, ItemStack heldStack) {
                this.menu = menu;
                this.heldStack = heldStack;
            }

            @Override
            protected void tryDispatch(ItemStack newHeldStack) {
                if (!areItemsSimilar(heldStack, newHeldStack)) return;
                int amount = newHeldStack.getCount() - heldStack.getCount();
                if (amount <= 0) return;
                WynntilsMod.postEvent(new InventoryInteractionEvent(
                        menu, new InventoryInteraction.PickUpAll(newHeldStack.copyWithCount(amount))));
            }
        }

        class Place extends CaptureHeldAndSlot {
            private final ItemStack heldStack;

            public Place(AbstractContainerMenu menu, int slotNum, ItemStack heldStack) {
                super(menu, slotNum);
                this.heldStack = heldStack;
            }

            @Override
            protected void tryDispatch(ItemStack newHeldStack, ItemStack newSlotStack) {
                if (!(newHeldStack.isEmpty() || areItemsSimilar(heldStack, newHeldStack))) return;
                if (!areItemsSimilar(heldStack, newSlotStack)) return;
                int amount = heldStack.getCount() - newHeldStack.getCount();
                if (amount <= 0) return;
                WynntilsMod.postEvent(new InventoryInteractionEvent(
                        menu, new InventoryInteraction.Place(slotNum, newSlotStack.copyWithCount(amount))));
            }
        }

        class Spread extends CaptureHeld {
            private final AbstractContainerMenu menu;
            private final IntList slots;
            private final ItemStack heldStack;
            private final boolean single;

            public Spread(AbstractContainerMenu menu, IntList slots, ItemStack heldStack, boolean single) {
                this.menu = menu;
                this.slots = slots;
                this.heldStack = heldStack;
                this.single = single;
            }

            @Override
            protected void tryDispatch(ItemStack newHeldStack) {
                if (!(newHeldStack.isEmpty() || areItemsSimilar(heldStack, newHeldStack))) return;
                int amount = heldStack.getCount() - newHeldStack.getCount();
                if (amount <= 0) return;
                WynntilsMod.postEvent(new InventoryInteractionEvent(
                        menu, new InventoryInteraction.Spread(slots, newHeldStack.copyWithCount(amount), single)));
            }
        }

        class Swap extends CaptureHeldAndSlot {
            private final ItemStack heldStack;
            private final ItemStack slotStack;

            public Swap(AbstractContainerMenu menu, int slotNum, ItemStack heldStack, ItemStack slotStack) {
                super(menu, slotNum);
                this.heldStack = heldStack;
                this.slotStack = slotStack;
            }

            @Override
            protected void tryDispatch(ItemStack newHeldStack, ItemStack newSlotStack) {
                if (!areStacksSimilar(heldStack, newSlotStack)) return;
                if (newHeldStack.isEmpty()) { // Was a placeholder item (e.g. an accessory slot)
                    WynntilsMod.postEvent(new InventoryInteractionEvent(
                            menu, new InventoryInteraction.Place(slotNum, newSlotStack.copy())));
                } else if (areStacksSimilar(slotStack, newHeldStack)) {
                    WynntilsMod.postEvent(new InventoryInteractionEvent(
                            menu, new InventoryInteraction.Swap(slotNum, newSlotStack.copy(), newHeldStack.copy())));
                }
            }
        }

        class ThrowFromHeld extends CaptureHeld {
            private final AbstractContainerMenu menu;
            private final ItemStack heldStack;

            public ThrowFromHeld(AbstractContainerMenu menu, ItemStack heldStack) {
                this.menu = menu;
                this.heldStack = heldStack;
            }

            @Override
            protected void tryDispatch(ItemStack newHeldStack) {
                if (!(newHeldStack.isEmpty() || areItemsSimilar(heldStack, newHeldStack))) return;
                int amount = heldStack.getCount() - newHeldStack.getCount();
                if (amount <= 0) return;
                WynntilsMod.postEvent(new InventoryInteractionEvent(
                        menu, new InventoryInteraction.ThrowFromHeld(newHeldStack.copyWithCount(amount))));
            }
        }

        class ThrowFromSlot extends CaptureSlot {
            private final ItemStack slotStack;

            public ThrowFromSlot(AbstractContainerMenu menu, int slotNum, ItemStack slotStack) {
                super(menu, slotNum);
                this.slotStack = slotStack;
            }

            @Override
            protected void tryDispatch(ItemStack newSlotStack) {
                if (!(newSlotStack.isEmpty() || areItemsSimilar(slotStack, newSlotStack))) return;
                int amount = slotStack.getCount() - newSlotStack.getCount();
                if (amount <= 0) return;
                WynntilsMod.postEvent(new InventoryInteractionEvent(
                        menu, new InventoryInteraction.ThrowFromSlot(slotNum, newSlotStack.copyWithCount(amount))));
            }
        }

        class Transfer extends CaptureSlot {
            private final ItemStack slotStack;

            public Transfer(AbstractContainerMenu menu, int slotNum, ItemStack slotStack) {
                super(menu, slotNum);
                this.slotStack = slotStack;
            }

            @Override
            protected void tryDispatch(ItemStack newSlotStack) {
                if (!(newSlotStack.isEmpty() || areItemsSimilar(slotStack, newSlotStack))) return;
                int amount = slotStack.getCount() - newSlotStack.getCount();
                if (amount <= 0) return;
                WynntilsMod.postEvent(new InventoryInteractionEvent(
                        menu, new InventoryInteraction.Transfer(slotNum, newSlotStack.copyWithCount(amount))));
            }
        }
    }

    private static final class PendingInteractionEntry {
        private final PendingInteraction interaction;
        private final long expiryTime;

        private PendingInteractionEntry(PendingInteraction interaction) {
            this.interaction = interaction;
            this.expiryTime = System.currentTimeMillis() + 500L;
        }
    }
}
