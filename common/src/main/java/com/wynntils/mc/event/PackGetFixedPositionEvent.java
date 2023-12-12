/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.eventbus.api.Event;

public class PackGetFixedPositionEvent extends Event {
    private final Pack pack;
    private boolean fixedPosition;

    public PackGetFixedPositionEvent(Pack pack, boolean fixedPosition) {
        this.pack = pack;
        this.fixedPosition = fixedPosition;
    }

    public Pack getPack() {
        return pack;
    }

    public boolean isFixedPosition() {
        return fixedPosition;
    }

    public void setFixedPosition(boolean fixedPosition) {
        this.fixedPosition = fixedPosition;
    }
}
