/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.wc.ModelLoader;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * This is a "high-quality misc" class. Helper methods that are commonly used throughout the project
 * without an aspect on minecraft can be put here. Keep the names short, but distinct.
 */
public class Utils {
    public static IEventBus getEventBus() {
        return WynntilsMod.getEventBus();
    }

    public static boolean onServer() {
        return ModelLoader.getWorldState().onServer();
    }

    public static boolean onWorld() {
        return ModelLoader.getWorldState().onWorld();
    }
}
