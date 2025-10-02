/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;

public class ChatSentEvent extends BaseEvent implements OperationCancelable {
    private final String message;

    public ChatSentEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
