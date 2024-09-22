/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.inventory.event.InventoryInteractionEvent;
import com.wynntils.utils.type.Confidence;
import com.wynntils.utils.wynn.ItemUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

interface PendingInteraction {
    boolean onSlotUpdate(int containerId, int slotNum, ItemStack newStack);

    default boolean onContentUpdate(int containerId, List<ItemStack> newInv) {
        return true;
    }

    abstract class CaptureHeld implements PendingInteraction {
        @Override
        public boolean onSlotUpdate(int containerId, int slotNum, ItemStack newStack) {
            if (containerId == -1) {
                checkAndSend(newStack);
                return false;
            }
            return true;
        }

        protected abstract void checkAndSend(ItemStack newHeldStack);
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
                checkAndSend(newStack);
                return false;
            }
            return true;
        }

        @Override
        public boolean onContentUpdate(int containerId, List<ItemStack> newInv) {
            if (containerId == menu.containerId && slotNum < newInv.size()) {
                checkAndSend(newInv.get(slotNum));
                return false;
            }
            return true;
        }

        protected abstract void checkAndSend(ItemStack newSlotStack);
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
            checkAndSend(newHeldStack, newSlotStack);
            return false;
        }

        protected abstract void checkAndSend(ItemStack newHeldStack, ItemStack newSlotStack);
    }

    class PickUp extends CaptureHeldAndSlot {
        private final ItemStack slotStack;

        public PickUp(AbstractContainerMenu menu, int slotNum, ItemStack slotStack) {
            super(menu, slotNum);
            this.slotStack = slotStack;
        }

        @Override
        protected void checkAndSend(ItemStack newHeldStack, ItemStack newSlotStack) {
            if (!ItemUtils.areItemsSimilar(slotStack, newHeldStack)) return;
            if (!(newSlotStack.isEmpty() || ItemUtils.areItemsSimilar(slotStack, newSlotStack))) return;
            WynntilsMod.postEvent(new InventoryInteractionEvent(
                    menu,
                    new InventoryInteraction.PickUp(slotNum, newHeldStack.copy(), newSlotStack.copy()),
                    Confidence.CERTAIN));
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
        protected void checkAndSend(ItemStack newHeldStack) {
            if (!ItemUtils.areItemsSimilar(heldStack, newHeldStack)) return;
            int amount = newHeldStack.getCount() - heldStack.getCount();
            if (amount <= 0) return;
            WynntilsMod.postEvent(new InventoryInteractionEvent(
                    menu, new InventoryInteraction.PickUpAll(newHeldStack.copyWithCount(amount)), Confidence.CERTAIN));
        }
    }

    class PlaceOrSwap extends CaptureHeldAndSlot {
        private final ItemStack heldStack;
        private final ItemStack slotStack;

        public PlaceOrSwap(AbstractContainerMenu menu, int slotNum, ItemStack heldStack, ItemStack slotStack) {
            super(menu, slotNum);
            this.heldStack = heldStack;
            this.slotStack = slotStack;
        }

        @Override
        protected void checkAndSend(ItemStack newHeldStack, ItemStack newSlotStack) {
            if (!ItemUtils.areItemsSimilar(heldStack, newSlotStack)) return;

            // Check for placement
            if (newHeldStack.isEmpty() || ItemUtils.areItemsSimilar(heldStack, newHeldStack)) {
                int numPlaced = heldStack.getCount() - newHeldStack.getCount();
                if (numPlaced > 0) {
                    WynntilsMod.postEvent(new InventoryInteractionEvent(
                            menu,
                            new InventoryInteraction.Place(slotNum, newSlotStack.copyWithCount(numPlaced)),
                            Confidence.CERTAIN));
                    return;
                }
            }

            // Check for swap
            if (!ItemUtils.areItemsSimilar(slotStack, newHeldStack)
                    || slotStack.getCount() != newHeldStack.getCount()
                    || heldStack.getCount() != newSlotStack.getCount()) return;
            WynntilsMod.postEvent(new InventoryInteractionEvent(
                    menu,
                    new InventoryInteraction.Swap(slotNum, newSlotStack.copy(), newHeldStack.copy()),
                    Confidence.CERTAIN));
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
        protected void checkAndSend(ItemStack newHeldStack) {
            if (!(newHeldStack.isEmpty() || ItemUtils.areItemsSimilar(heldStack, newHeldStack))) return;
            int amount = heldStack.getCount() - newHeldStack.getCount();
            if (amount <= 0) return;
            WynntilsMod.postEvent(new InventoryInteractionEvent(
                    menu,
                    new InventoryInteraction.Spread(slots, heldStack.copyWithCount(amount), single),
                    Confidence.CERTAIN));
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
        protected void checkAndSend(ItemStack newHeldStack) {
            if (!(newHeldStack.isEmpty() || ItemUtils.areItemsSimilar(heldStack, newHeldStack))) return;
            int amount = heldStack.getCount() - newHeldStack.getCount();
            if (amount <= 0) return;
            WynntilsMod.postEvent(new InventoryInteractionEvent(
                    menu, new InventoryInteraction.ThrowFromHeld(heldStack.copyWithCount(amount)), Confidence.CERTAIN));
        }
    }

    class ThrowFromSlot extends CaptureSlot {
        private final ItemStack slotStack;

        public ThrowFromSlot(AbstractContainerMenu menu, int slotNum, ItemStack slotStack) {
            super(menu, slotNum);
            this.slotStack = slotStack;
        }

        @Override
        protected void checkAndSend(ItemStack newSlotStack) {
            if (!(newSlotStack.isEmpty() || ItemUtils.areItemsSimilar(slotStack, newSlotStack))) return;
            int amount = slotStack.getCount() - newSlotStack.getCount();
            if (amount <= 0) return;
            WynntilsMod.postEvent(new InventoryInteractionEvent(
                    menu,
                    new InventoryInteraction.ThrowFromSlot(slotNum, slotStack.copyWithCount(amount)),
                    Confidence.CERTAIN));
        }
    }

    class Transfer extends CaptureSlot {
        private final ItemStack slotStack;

        public Transfer(AbstractContainerMenu menu, int slotNum, ItemStack slotStack) {
            super(menu, slotNum);
            this.slotStack = slotStack;
        }

        @Override
        protected void checkAndSend(ItemStack newSlotStack) {
            if (!(newSlotStack.isEmpty() || ItemUtils.areItemsSimilar(slotStack, newSlotStack))) return;
            int amount = slotStack.getCount() - newSlotStack.getCount();
            if (amount <= 0) return;
            WynntilsMod.postEvent(new InventoryInteractionEvent(
                    menu,
                    new InventoryInteraction.Transfer(slotNum, slotStack.copyWithCount(amount)),
                    Confidence.CERTAIN));
        }
    }
}
