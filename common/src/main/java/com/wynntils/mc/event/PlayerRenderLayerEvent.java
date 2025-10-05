/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class PlayerRenderLayerEvent extends Event implements ICancellableEvent {
    private final AvatarRenderState avatarRenderState;

    protected PlayerRenderLayerEvent(AvatarRenderState avatarRenderState) {
        this.avatarRenderState = avatarRenderState;
    }

    public AvatarRenderState getAvatarRenderState() {
        return avatarRenderState;
    }

    public static class Armor extends PlayerRenderLayerEvent {
        private final EquipmentSlot slot;

        public Armor(AvatarRenderState avatarRenderState, EquipmentSlot slot) {
            super(avatarRenderState);
            this.slot = slot;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }
    }

    public static class Cape extends PlayerRenderLayerEvent {
        public Cape(AvatarRenderState avatarRenderState) {
            super(avatarRenderState);
        }
    }

    public static class Elytra extends PlayerRenderLayerEvent {
        public Elytra(AvatarRenderState avatarRenderState) {
            super(avatarRenderState);
        }
    }
}
