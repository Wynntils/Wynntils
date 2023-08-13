/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class UseItemEvent extends PlayerEvent {
    private final Level level;
    private final InteractionHand hand;

    public UseItemEvent(Player player, Level level, InteractionHand hand) {
        super(player);
        this.level = level;
        this.hand = hand;
    }

    public Level getLevel() {
        return level;
    }

    public InteractionHand getHand() {
        return hand;
    }
}
