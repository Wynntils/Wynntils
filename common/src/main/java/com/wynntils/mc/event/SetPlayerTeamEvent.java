/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class SetPlayerTeamEvent extends Event {
    private final int method;
    private final String teamName;

    public SetPlayerTeamEvent(int method, String teamName) {
        this.method = method;
        this.teamName = teamName;
    }

    public int getMethod() {
        return method;
    }

    public String getTeamName() {
        return teamName;
    }
}
