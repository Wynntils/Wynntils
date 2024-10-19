/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class KeyMappingEvent extends Event implements ICancellableEvent {
    private final InputConstants.Key key;
    private final Operation operation;

    public KeyMappingEvent(InputConstants.Key key, Operation operation) {
        this.key = key;
        this.operation = operation;
    }

    public InputConstants.Key getKey() {
        return key;
    }

    public Operation getOperation() {
        return operation;
    }

    public enum Operation {
        SET,
        UNSET,
        CLICK
    }
}
