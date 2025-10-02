/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.CancelRequestable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class UseItemEvent extends PlayerEvent implements CancelRequestable {
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
