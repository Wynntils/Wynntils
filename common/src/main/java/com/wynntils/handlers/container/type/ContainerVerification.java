/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.type;

import com.wynntils.models.containers.Container;

@FunctionalInterface
public interface ContainerVerification {
    boolean verify(Class<? extends Container> containerType);
}
