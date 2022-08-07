/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc;

import com.wynntils.core.WynntilsMod;
import com.wynntils.wc.model.Character;
import com.wynntils.wc.model.WorldState;
import java.util.Arrays;

public abstract class ModelLoader {
    private static final WorldState worldState = new WorldState();
    private static final Character character = new Character();

    private static final Model[] MODELS = {worldState, character};

    public static WorldState getWorldState() {
        return worldState;
    }

    public static Character getCharacter() {
        return character;
    }

    public static void init() {
        Arrays.stream(MODELS).forEach(model -> {
            WynntilsMod.getEventBus().register(model);
            WynntilsMod.getEventBus().register(model.getClass());
        });
    }
}
