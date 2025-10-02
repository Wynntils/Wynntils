/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;

public class KeyMappingEvent extends BaseEvent implements OperationCancelable {
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
