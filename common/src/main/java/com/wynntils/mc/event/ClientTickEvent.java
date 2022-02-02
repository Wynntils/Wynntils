package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public class ClientTickEvent extends Event {
    private final Phase phase;

    public ClientTickEvent(Phase phase) {
        this.phase = phase;
    }

    public Phase getTickPhase() {
        return phase;
    }

    public enum Phase {
        START,
        END
    }
}
