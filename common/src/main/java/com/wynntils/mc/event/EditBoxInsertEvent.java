/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class EditBoxInsertEvent extends Event {
    private final String textToWrite;

    public EditBoxInsertEvent(String textToWrite) {
        this.textToWrite = textToWrite;
    }

    public String getTextToWrite() {
        return textToWrite;
    }
}
