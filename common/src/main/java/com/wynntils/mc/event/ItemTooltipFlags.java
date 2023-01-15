/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public abstract class ItemTooltipFlags extends WynntilsEvent {
    private final ItemStack itemStack;

    protected ItemTooltipFlags(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static final class Advanced extends ItemTooltipFlags {
        private TooltipFlag flags;

        public Advanced(ItemStack itemStack, TooltipFlag flags) {
            super(itemStack);
            this.flags = flags;
        }

        public TooltipFlag getFlags() {
            return flags;
        }

        public void setFlags(TooltipFlag flags) {
            this.flags = flags;
        }
    }

    public static final class Mask extends ItemTooltipFlags {
        private int mask;

        public Mask(ItemStack itemStack, int mask) {
            super(itemStack);
            this.mask = mask;
        }

        public int getMask() {
            return mask;
        }

        public void setMask(int mask) {
            this.mask = mask;
        }
    }
}
