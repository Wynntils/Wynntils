/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.type;

@FunctionalInterface
public interface StartAction {
    boolean execute(ContainerContent container);
}
