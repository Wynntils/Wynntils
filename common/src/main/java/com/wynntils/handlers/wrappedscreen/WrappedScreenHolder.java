/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.wrappedscreen;

import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import java.lang.reflect.ParameterizedType;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;

/**
 * A class that holds all logic for a wrapped screen. This class is also a factory for the wrapped screen.
 * This class is only registered to the event bus when active. Every logic that needs to be done on a wrapped screen should be done here.
 * This class may depend on {@link com.wynntils.core.components.Model}s, {@link com.wynntils.core.components.Handler}s and {@link com.wynntils.core.components.Services}s.
 */
public abstract class WrappedScreenHolder<T extends Screen & WrappedScreen> {
    protected abstract Pattern getReplacedScreenTitlePattern();

    /**
     * @param wrappedScreenInfo
     * @return The wrapped screen.
     */
    protected abstract T createWrappedScreen(WrappedScreenInfo wrappedScreenInfo);

    /**
     * Called when a wrapped screen is opened. This method should initialize the state of this class.
     * @param wrappedScreen The wrapped screen that was opened.
     */
    protected abstract void setWrappedScreen(T wrappedScreen);

    /**
     * Called when a wrapped screen is closed. This method should reset the state of this class.
     */
    protected abstract void reset();

    /**
     * @return The class of the wrapped screen.
     */
    protected Class<T> getWrappedScreenClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
