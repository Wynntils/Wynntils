/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.horse;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class MountModel extends Model {
    public MountModel() {
        super(List.of());
    }

    public Optional<MountItem> getMount() {
        int mountSlot = findMountSlotNum();
        if (mountSlot == -1) return Optional.empty();

        return Models.Item.asWynnItem(McUtils.inventory().getItem(mountSlot), MountItem.class);
    }

    public int findMountSlotNum() {
        Inventory inventory = McUtils.inventory();
        for (int slotNum = 0; slotNum < Inventory.INVENTORY_SIZE; slotNum++) {
            ItemStack itemStack = inventory.getItem(slotNum);
            if (Models.Item.asWynnItem(itemStack, MountItem.class).isPresent()) {
                return slotNum;
            }
        }
        return -1;
    }
}
