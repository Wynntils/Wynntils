/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.event;

import com.wynntils.models.abilities.type.AbilityCooldown;
import java.util.Set;
import net.neoforged.bus.api.Event;

public class AbilityCooldownsUpdatedEvent extends Event {
    private final Set<AbilityCooldown> cooldowns;

    public AbilityCooldownsUpdatedEvent(Set<AbilityCooldown> cooldowns) {
        this.cooldowns = cooldowns;
    }

    public Set<AbilityCooldown> getCooldowns() {
        return cooldowns;
    }
}
