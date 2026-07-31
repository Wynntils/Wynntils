/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects.event;

import com.wynntils.models.statuseffects.type.StatusEffect;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.bus.api.Event;

public class StatusEffectsChangedEvent extends Event {
    private final List<StatusEffect> originalStatusEffects;
    private List<StatusEffect> statusEffects;

    public StatusEffectsChangedEvent(List<StatusEffect> statusEffects) {
        this.originalStatusEffects = List.copyOf(statusEffects);
        this.statusEffects = new ArrayList<>(statusEffects);
    }

    public void removeStatusEffect(StatusEffect statusEffect) {
        statusEffects.remove(statusEffect);
    }

    public List<StatusEffect> getStatusEffects() {
        return statusEffects;
    }

    public List<StatusEffect> getOriginalStatusEffects() {
        return originalStatusEffects;
    }
}
