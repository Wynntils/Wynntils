/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

public class AdvancementUpdateEvent extends Event {
    private final boolean reset;
    private final List<AdvancementHolder> added;
    private final Set<Identifier> removed;
    private final Map<Identifier, AdvancementProgress> progress;

    public AdvancementUpdateEvent(
            boolean reset,
            List<AdvancementHolder> added,
            Set<Identifier> removed,
            Map<Identifier, AdvancementProgress> progress) {
        this.reset = reset;
        this.added = added;
        this.removed = removed;
        this.progress = progress;
    }

    public boolean isReset() {
        return reset;
    }

    public List<AdvancementHolder> getAdded() {
        return added;
    }

    public Set<Identifier> getRemoved() {
        return removed;
    }

    public Map<Identifier, AdvancementProgress> getProgress() {
        return progress;
    }
}
