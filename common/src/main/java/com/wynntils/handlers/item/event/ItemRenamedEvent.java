/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item.event;

import net.minecraftforge.eventbus.api.Event;

public class ItemRenamedEvent extends Event {
    private final String oldName;
    private final String newName;

    public ItemRenamedEvent(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }
}
