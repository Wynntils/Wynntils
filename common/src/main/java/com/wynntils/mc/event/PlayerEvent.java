/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.entity.player.Player;

public abstract class PlayerEvent extends LivingEvent {
    private final Player entityPlayer;

    protected PlayerEvent(Player player) {
        super(player);
        this.entityPlayer = player;
    }

    public Player getPlayer() {
        return this.entityPlayer;
    }
}
