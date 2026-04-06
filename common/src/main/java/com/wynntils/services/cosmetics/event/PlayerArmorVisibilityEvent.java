/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.cosmetics.event;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.Event;

public class PlayerArmorVisibilityEvent extends Event {
    private final EquipmentSlot slot;
    private final AvatarRenderState renderState;

    private boolean isVisible = true;

    public PlayerArmorVisibilityEvent(EquipmentSlot slot, AvatarRenderState renderState) {
        this.slot = slot;
        this.renderState = renderState;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public AvatarRenderState getRenderState() {
        return renderState;
    }
}
