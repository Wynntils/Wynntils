/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

public final class Workarounds {
    // These workarounds are temporary, awaiting the full refactoringsation
    public static void init() {
        scoreboardHandlerWorkarounds();
        // A lot of models depend on this.
        Models.Item.init();
    }

    private static void scoreboardHandlerWorkarounds() {
        // FIXME: Need ugly workarounds for init until all model refactoring is complete
        Managers.Quest.initWorkaround();

        // FIXME: ScoreboardHandler should be able to init ifself upon listener registration
        Handlers.Scoreboard.init();
    }
}
