/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.type;

import com.wynntils.handlers.container.ContainerQueryException;

@FunctionalInterface
public interface ContainerAction {
    void processContainer(ContainerContent container) throws ContainerQueryException;
}
