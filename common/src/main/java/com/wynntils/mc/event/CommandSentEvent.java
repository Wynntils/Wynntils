/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class CommandSentEvent extends Event {
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
