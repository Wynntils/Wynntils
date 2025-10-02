/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.CancelRequestable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PlayerAttackEvent extends PlayerEvent implements CancelRequestable {
    private final Entity target;

    public PlayerAttackEvent(Player player, Entity target) {
        super(player);
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }
}
