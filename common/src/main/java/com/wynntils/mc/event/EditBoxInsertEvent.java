/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.neoforged.bus.api.ICancellableEvent;

public class EditBoxInsertEvent extends BaseEvent implements ICancellableEvent {
    private final String textToWrite;

    public EditBoxInsertEvent(String textToWrite) {
        this.textToWrite = textToWrite;
    }

    public String getTextToWrite() {
        return textToWrite;
    }
}
