/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.BossBarHandler;
import com.wynntils.handlers.chat.ChatHandler;
import com.wynntils.handlers.container.ContainerQueryHandler;
import com.wynntils.handlers.scoreboard.ScoreboardHandler;
import org.apache.commons.lang3.reflect.FieldUtils;

public final class Handlers {
    public static final BossBarHandler BossBar = new BossBarHandler();
    public static final ChatHandler Chat = new ChatHandler();
    public static final ContainerQueryHandler ContainerQuery = new ContainerQueryHandler();
    public static final ScoreboardHandler Scoreboard = new ScoreboardHandler();

    public static void init() {
        // Register all handler singletons as event listeners

        FieldUtils.getAllFieldsList(Handlers.class).stream()
                .filter(field -> Handler.class.isAssignableFrom(field.getType()))
                .forEach(field -> {
                    try {
                        WynntilsMod.registerEventListener(field.get(null));
                    } catch (IllegalAccessException e) {
                        WynntilsMod.error("Internal error in Handlers", e);
                        throw new RuntimeException(e);
                    }
                });
    }
}
