/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.wrappedscreen.type;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * This class holds the information of a screen that we wrap with {@link com.wynntils.handlers.wrappedscreen.WrappedScreen}s.
 */
public record WrappedScreenInfo(
        AbstractContainerScreen<?> screen, AbstractContainerMenu containerMenu, int containerId) {}
