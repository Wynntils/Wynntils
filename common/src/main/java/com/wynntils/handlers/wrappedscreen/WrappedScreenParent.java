/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.wrappedscreen;

import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * A class that holds all logic for a wrapped screen. This class is also a factory for the wrapped screen.
 * This class is only registered to the event bus when active. Every logic that needs to be done on a wrapped screen should be done here.
 * This class may depend on {@link com.wynntils.core.components.Model}s, {@link com.wynntils.core.components.Handler}s and {@link com.wynntils.core.components.Services}s.
 */
public interface WrappedScreenParent<T extends WrappedScreen> {
    /**
     * @return The pattern that matches the title of the screen that should be replaced.
     */
    Pattern getReplacedScreenTitlePattern();

    /**
     * @param originalScreen        The original screen that should be wrapped.
     * @param abstractContainerMenu
     * @param containerId           The container id of the original screen.
     * @return The wrapped screen.
     */
    T createWrappedScreen(Screen originalScreen, AbstractContainerMenu abstractContainerMenu, int containerId);

    /**
     * Called when a wrapped screen is opened. This method should initialize the state of this class.
     * @param wrappedScreen The wrapped screen that was opened.
     */
    void setWrappedScreen(T wrappedScreen);

    /**
     * Called when a wrapped screen is closed. This method should reset the state of this class.
     */
    void reset();
}
