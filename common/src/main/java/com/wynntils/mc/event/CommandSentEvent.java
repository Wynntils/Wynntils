/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;

public final class CommandSentEvent extends BaseEvent implements CancelRequestable {
    private final String command;
    private final boolean signed;

    public CommandSentEvent(String command, boolean signed) {
        this.command = command;
        this.signed = signed;
    }

    public String getCommand() {
        return command;
    }

    public boolean isSigned() {
        return signed;
    }
}
