/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.damage.type;

import com.wynntils.models.stats.type.DamageType;
import java.util.Map;
import net.minecraftforge.eventbus.api.Event;

public final class DamageDealtEvent extends Event {
    private final Map<DamageType, Integer> damages;

    public DamageDealtEvent(Map<DamageType, Integer> damages) {
        this.damages = damages;
    }

    public Map<DamageType, Integer> getDamages() {
        return damages;
    }
}
