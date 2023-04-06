/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item.event;

import com.wynntils.core.text.StyledText2;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ItemRenamedEvent extends Event {
    private final ItemStack itemStack;
    private final StyledText2 oldName;
    private final StyledText2 newName;

    public ItemRenamedEvent(ItemStack itemStack, StyledText2 oldName, StyledText2 newName) {
        this.itemStack = itemStack;
        this.oldName = oldName;
        this.newName = newName;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public StyledText2 getOldName() {
        return oldName;
    }

    public StyledText2 getNewName() {
        return newName;
    }
}
