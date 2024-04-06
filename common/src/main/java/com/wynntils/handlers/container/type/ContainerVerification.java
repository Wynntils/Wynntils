/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.type;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;

@FunctionalInterface
public interface ContainerVerification {
    boolean verify(Component title, MenuType<?> menuType);
}
