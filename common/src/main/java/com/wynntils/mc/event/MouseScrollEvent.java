package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public class MouseScrollEvent extends Event {

    private final boolean isScrollingUp;


    public MouseScrollEvent(boolean isScrollingUp) {
        this.isScrollingUp = isScrollingUp;
    }

    public boolean isScrollingUp() { return isScrollingUp; }
}