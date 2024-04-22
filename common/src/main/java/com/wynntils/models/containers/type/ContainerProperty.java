/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import net.minecraft.client.gui.screens.Screen;

/**
 * Marker interface for properties that are used in containers.
 */
public interface ContainerProperty {
    /**
     * Sets the container ID.
     *
     * @param containerId The container ID.
     */
    void setContainerId(int containerId);

    /**
     * Gets the container ID.
     *
     * @return The container ID.
     */
    int getContainerId();

    /**
     * Checks if the screen is the container.
     *
     * @param screen The screen to check.
     * @return True if the screen is the container, false otherwise.
     */
    boolean isScreen(Screen screen);
}
