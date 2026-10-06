/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.type;

import com.wynntils.utils.mc.type.Location;
import net.neoforged.bus.api.Event;

public final class MobKillEvent extends Event {
    private final String mobName;
    private final KillCreditType killCredit;
    private final Location location;

    public MobKillEvent(String name, KillCreditType type, Location loc) {
        mobName = name;
        killCredit = type;
        location = loc;
    }

    public String getMobName() {
        return mobName;
    }

    public KillCreditType getKillCredit() {
        return killCredit;
    }

    public Location getLocation() {
        return location;
    }
}
