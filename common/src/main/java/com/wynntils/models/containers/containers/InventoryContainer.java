/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * This is the container for the player's inventory.
 */
public class InventoryContainer extends Container implements HighlightableProfessionProperty {
    public InventoryContainer() {
        super(screen -> screen instanceof InventoryScreen);
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(1, 0, 4, 9);
    }
}
