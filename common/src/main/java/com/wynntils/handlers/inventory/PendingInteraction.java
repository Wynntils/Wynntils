package com.wynntils.handlers.inventory;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.inventory.event.InventoryInteractionEvent;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

interface PendingInteraction {
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
            if (!InventoryHandler.areItemsSimilar(slotStack, newHeldStack)) return;
            if (!(newSlotStack.isEmpty() || InventoryHandler.areItemsSimilar(slotStack, newSlotStack))) return;
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
            if (!InventoryHandler.areItemsSimilar(heldStack, newHeldStack)) return;
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
            if (!(newHeldStack.isEmpty() || InventoryHandler.areItemsSimilar(heldStack, newHeldStack))) return;
            if (!InventoryHandler.areItemsSimilar(heldStack, newSlotStack)) return;
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
            if (!(newHeldStack.isEmpty() || InventoryHandler.areItemsSimilar(heldStack, newHeldStack))) return;
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
            if (!InventoryHandler.areStacksSimilar(heldStack, newSlotStack)) return;
            if (newHeldStack.isEmpty()) { // Was a placeholder item (e.g. an accessory slot)
                WynntilsMod.postEvent(new InventoryInteractionEvent(
                        menu, new InventoryInteraction.Place(slotNum, newSlotStack.copy())));
            } else if (InventoryHandler.areStacksSimilar(slotStack, newHeldStack)) {
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
            if (!(newHeldStack.isEmpty() || InventoryHandler.areItemsSimilar(heldStack, newHeldStack))) return;
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
            if (!(newSlotStack.isEmpty() || InventoryHandler.areItemsSimilar(slotStack, newSlotStack))) return;
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
            if (!(newSlotStack.isEmpty() || InventoryHandler.areItemsSimilar(slotStack, newSlotStack))) return;
            int amount = slotStack.getCount() - newSlotStack.getCount();
            if (amount <= 0) return;
            WynntilsMod.postEvent(new InventoryInteractionEvent(
                    menu, new InventoryInteraction.Transfer(slotNum, newSlotStack.copyWithCount(amount))));
        }
    }
}
