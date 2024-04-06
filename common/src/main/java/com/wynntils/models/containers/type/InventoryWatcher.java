/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public class InventoryWatcher {
    private int slots;
    private int totalCount;
    private final Predicate<ItemStack> checker;

    public InventoryWatcher(Predicate<ItemStack> checker) {
        this.checker = checker;
    }

    public int getSlots() {
        return slots;
    }

    public int getTotalCount() {
        return totalCount;
    }

    protected void onUpdate(int oldSlots, int oldTotalCount) {
        // Called after an update has been made, override if needed
    }

    // This should only be called by the model
    public boolean shouldInclude(ItemStack itemStack) {
        return checker.test(itemStack);
    }

    // This should only be called by the model
    public void updateFromModel(int slots, int totalCount) {
        int oldSlots = this.slots;
        int oldTotalCount = this.totalCount;
        this.slots = slots;
        this.totalCount = totalCount;
        onUpdate(oldSlots, oldTotalCount);
    }
}
