package com.wynntils.utils.type;

public enum ActionSpeed {
    FAST(4),
    BALANCED(5),
    SAFE(6),
    VERY_SAFE(8);

    private final int ticksDelay;

    ActionSpeed(int ticksDelay) {
        this.ticksDelay = ticksDelay;
    }

    public int getTicksDelay() {
        return ticksDelay;
    }
}
