/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.events;

public interface CancelRequestable {
    default void requestCancel() {
        ((BaseEvent) this).cancelRequested = true;
    }

    default boolean isCancelRequested() {
        return ((BaseEvent) this).cancelRequested;
    }
}
