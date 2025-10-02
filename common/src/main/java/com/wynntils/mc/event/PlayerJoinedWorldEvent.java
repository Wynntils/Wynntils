/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.world.entity.player.Player;

public class PlayerJoinedWorldEvent extends BaseEvent {
    private final Player player;

    public PlayerJoinedWorldEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
