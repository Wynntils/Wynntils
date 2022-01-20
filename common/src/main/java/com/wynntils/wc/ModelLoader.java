/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc;

import com.wynntils.utils.Utils;
import com.wynntils.wc.impl.CharacterImpl;
import com.wynntils.wc.impl.WorldStateImpl;
import com.wynntils.wc.model.Character;
import com.wynntils.wc.model.WorldState;
import java.util.Arrays;

public abstract class ModelLoader {
    private static final WorldState worldState = new WorldStateImpl();
    private static final Character character = new CharacterImpl();

    public static final Model[] MODELS = new Model[] {worldState, character};

    public static WorldState getWorldState() {
        return worldState;
    }

    public static Character getCharacter() {
        return character;
    }

    public static void init() {
        Arrays.stream(MODELS)
                .forEach(
                        model -> {
                            Utils.getEventBus().register(model);
                            Utils.getEventBus().register(model.getClass());
                        });
    }
}
