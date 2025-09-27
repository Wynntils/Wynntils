/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class ValuableFoundEvent extends Event {
    private final ItemStack item;
    private final ItemSource itemSource;

    public ValuableFoundEvent(ItemStack item, ItemSource itemSource) {
        this.item = item;
        this.itemSource = itemSource;
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemSource getItemSource() {
        return itemSource;
    }

    public enum ItemSource {
        LOOT_CHEST,
        LOOTRUN_REWARD_CHEST,
        RAID_REWARD_CHEST,
        WORLD_EVENT
    }
}
