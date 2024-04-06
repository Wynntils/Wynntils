/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
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

    /**
     * 0 = METHOD_ADD<p>
     * 1 = METHOD_REMOVE<p>
     * 2 = METHOD_CHANGE<p>
     * 3 = METHOD_JOIN<p>
     * 4 = METHOD_LEAVE
     */
    public int getMethod() {
        return method;
    }

    public String getTeamName() {
        return teamName;
    }
}
