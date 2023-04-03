/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item.event;

import com.wynntils.utils.mc.type.CodedString;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ItemRenamedEvent extends Event {
    private final ItemStack itemStack;
    private final CodedString oldName;
    private final CodedString newName;

    public ItemRenamedEvent(ItemStack itemStack, CodedString oldName, CodedString newName) {
        this.itemStack = itemStack;
        this.oldName = oldName;
        this.newName = newName;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public CodedString getOldName() {
        return oldName;
    }

    public CodedString getNewName() {
        return newName;
    }
}
