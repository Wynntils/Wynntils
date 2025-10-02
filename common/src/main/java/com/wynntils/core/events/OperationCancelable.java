/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.events;

public interface OperationCancelable {
    default void cancelOperation() {
        ((BaseEvent) this).isOperationCanceled = true;
    }

    default boolean isCanceled() {
        return ((BaseEvent) this).isOperationCanceled;
    }
}
