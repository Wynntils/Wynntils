/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.neoforged.bus.api.Event;

public class ShiftKeyStateChangeEvent extends Event {
    private final ServerboundPlayerCommandPacket.Action action;

    public ShiftKeyStateChangeEvent(ServerboundPlayerCommandPacket.Action action) {
        this.action = action;
    }

    public ServerboundPlayerCommandPacket.Action getAction() {
        return action;
    }
}
