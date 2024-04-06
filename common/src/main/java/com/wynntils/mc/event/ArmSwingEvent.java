/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.InteractionHand;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ArmSwingEvent extends Event {
    private final ArmSwingContext actionContext;

    private final InteractionHand hand;

    public ArmSwingEvent(ArmSwingContext actionContext, InteractionHand hand) {
        this.actionContext = actionContext;
        this.hand = hand;
    }

    public ArmSwingContext getActionContext() {
        return actionContext;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public enum ArmSwingContext {
        DROP_ITEM_FROM_INVENTORY_SCREEN,

        // Not implemented
        //        DROP_ITEM_FROM_HOTBAR,
        //        ATTACK_OR_START_BREAKING_BLOCK,
        //        BREAKING_BLOCK,
        //        INTERACT_WITH_BLOCK,
        //        INTERACT_WITH_ENTITY,
        //        USE_ITEM,
    }
}
