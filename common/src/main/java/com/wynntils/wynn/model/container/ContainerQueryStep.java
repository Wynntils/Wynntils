/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.container;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;

public interface ContainerQueryStep {
    boolean startStep(ContainerContent container);

    boolean verifyContainer(Component title, MenuType menuType);

    void handleContent(ContainerContent container);

    ContainerQueryStep getNextStep(ContainerContent container);

    void onError(String errorMsg);

    /** A way to identify this query */
    String getName();
}
