/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.utils.mc.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.INVENTORY)
public class ImprovedInventorySyncFeature extends Feature {
    @Persisted
    public Config<Boolean> forceSync = new Config<>(false);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onContainerClick(ContainerClickEvent event) {
        if (!forceSync.get()) return;
        event.setCanceled(true);
        AbstractContainerMenu menu = event.getContainerMenu();

        // Store old inventory state
        ItemStack oldHeld = menu.getCarried().copy();
        ItemStack[] oldItems = new ItemStack[menu.slots.size()];
        for (int i = 0; i < oldItems.length; i++) {
            oldItems[i] = menu.slots.get(i).getItem().copy();
        }

        // Perform click
        menu.clicked(event.getSlotNum(), event.getMouseButton(), event.getClickType(), McUtils.player());

        // Detect changes
        Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < oldItems.length; i++) {
            ItemStack newItem = menu.slots.get(i).getItem();
            if (!ItemStack.matches(oldItems[i], newItem)) {
                changedSlots.put(i, newItem.copy());
            }
        }

        // Send click packet
        McUtils.sendPacket(new ServerboundContainerClickPacket(
                menu.containerId,
                menu.getStateId(),
                event.getSlotNum(),
                event.getMouseButton(),
                event.getClickType(),
                menu.getCarried().copy(),
                changedSlots));

        // Restore previous inventory state in expectation of an update from the server (see InventoryHandler)
        menu.initializeContents(menu.getStateId(), Arrays.asList(oldItems), oldHeld);
    }
}
