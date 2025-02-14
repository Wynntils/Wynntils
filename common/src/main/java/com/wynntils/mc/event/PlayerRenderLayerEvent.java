/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class PlayerRenderLayerEvent extends Event implements ICancellableEvent {
    private final PlayerRenderState playerRenderState;

    protected PlayerRenderLayerEvent(PlayerRenderState playerRenderState) {
        this.playerRenderState = playerRenderState;
    }

    public PlayerRenderState getPlayerRenderState() {
        return playerRenderState;
    }

    public static class Armor extends PlayerRenderLayerEvent {
        private final EquipmentSlot slot;

        public Armor(PlayerRenderState playerRenderState, EquipmentSlot slot) {
            super(playerRenderState);
            this.slot = slot;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }
    }

    public static class Cape extends PlayerRenderLayerEvent {
        public Cape(PlayerRenderState playerRenderState) {
            super(playerRenderState);
        }
    }

    public static class Elytra extends PlayerRenderLayerEvent {
        public Elytra(PlayerRenderState playerRenderState) {
            super(playerRenderState);
        }
    }
}
