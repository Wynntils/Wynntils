/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model;

import com.wynntils.WynntilsMod;
import java.util.Arrays;

public abstract class Model {
    public static final Model[] MODELS = new Model[] {new WorldState()};

    public static void init() {
        Arrays.stream(MODELS)
                .forEach(
                        model -> {
                            WynntilsMod.EVENT_BUS.register(model);
                            WynntilsMod.EVENT_BUS.register(model.getClass());
                        });
    }
}
