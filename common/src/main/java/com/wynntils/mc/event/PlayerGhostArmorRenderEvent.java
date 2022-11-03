/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PlayerGhostArmorRenderEvent extends Event {
    private final Player player;

    public PlayerGhostArmorRenderEvent(Player p) {
        this.player = p;
    }

    public Player getPlayer() {
        return player;
    }
}
