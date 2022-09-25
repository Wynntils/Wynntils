package com.wynntils.mc.event;

import com.wynntils.wynn.model.bossbar.TrackedBar;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class CustomBarAddEvent extends Event {
    private final TrackedBar.BarType type;

    public CustomBarAddEvent(TrackedBar.BarType type) {
        this.type = type;
    }

    public TrackedBar.BarType getType() {
        return type;
    }
}
