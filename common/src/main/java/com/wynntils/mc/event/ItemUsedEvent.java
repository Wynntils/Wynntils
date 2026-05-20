package com.wynntils.mc.event;

import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class ItemUsedEvent extends Event implements ICancellableEvent {
    private InteractionHand hand;

    public ItemUsedEvent(InteractionHand hand) {
        this.hand = hand;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public void setHand(InteractionHand hand) {
        this.hand = hand;
    }
}
