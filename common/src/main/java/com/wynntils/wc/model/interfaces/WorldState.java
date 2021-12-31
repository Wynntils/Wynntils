/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.model.interfaces;

import com.wynntils.wc.model.Model;

public interface WorldState extends Model {
    boolean onServer();

    boolean onWorld();

    boolean isInStream();

    boolean isOnBetaServer();

    State getCurrentState();

    enum State {
        NOT_CONNECTED,
        CONNECTING,
        INTERIM,
        HUB,
        CHARACTER_SELECTION,
        WORLD
    }
}
