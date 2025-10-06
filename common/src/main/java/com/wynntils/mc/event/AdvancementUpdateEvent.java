/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;

public final class AdvancementUpdateEvent extends BaseEvent {
    private final boolean reset;
    private final List<AdvancementHolder> added;
    private final Set<ResourceLocation> removed;
    private final Map<ResourceLocation, AdvancementProgress> progress;

    public AdvancementUpdateEvent(
            boolean reset,
            List<AdvancementHolder> added,
            Set<ResourceLocation> removed,
            Map<ResourceLocation, AdvancementProgress> progress) {
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

    public Set<ResourceLocation> getRemoved() {
        return removed;
    }

    public Map<ResourceLocation, AdvancementProgress> getProgress() {
        return progress;
    }
}
