/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ItemRenamedEvent extends Event {
    private final ItemStack itemStack;
    private final String oldName;
    private final String newName;

    public ItemRenamedEvent(ItemStack itemStack, String oldName, String newName) {
        this.itemStack = itemStack;
        this.oldName = oldName;
        this.newName = newName;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }
}
