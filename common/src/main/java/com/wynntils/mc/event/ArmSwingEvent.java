/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;
import net.minecraft.world.InteractionHand;

public class ArmSwingEvent extends BaseEvent implements OperationCancelable {
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
        ATTACK_OR_START_BREAKING_BLOCK,

        // Not implemented
        //        DROP_ITEM_FROM_HOTBAR,
        //        BREAKING_BLOCK,
        //        INTERACT_WITH_BLOCK,
        //        INTERACT_WITH_ENTITY,
        //        USE_ITEM,
    }
}
