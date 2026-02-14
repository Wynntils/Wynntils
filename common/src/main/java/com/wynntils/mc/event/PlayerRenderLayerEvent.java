/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class PlayerRenderLayerEvent extends Event implements ICancellableEvent {
    private final HumanoidRenderState humanoidRenderState;

    protected PlayerRenderLayerEvent(HumanoidRenderState humanoidRenderState) {
        this.humanoidRenderState = humanoidRenderState;
    }

    public HumanoidRenderState getHumanoidRenderState() {
        return humanoidRenderState;
    }

    public static class Armor extends PlayerRenderLayerEvent {
        private final EquipmentSlot slot;

        public Armor(HumanoidRenderState humanoidRenderState, EquipmentSlot slot) {
            super(humanoidRenderState);
            this.slot = slot;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }
    }

    public static class Cape extends PlayerRenderLayerEvent {
        public Cape(HumanoidRenderState humanoidRenderState) {
            super(humanoidRenderState);
        }
    }

    public static class Elytra extends PlayerRenderLayerEvent {
        public Elytra(HumanoidRenderState humanoidRenderState) {
            super(humanoidRenderState);
        }
    }
}
