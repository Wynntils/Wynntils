/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

public class AdvancementUpdateEvent extends Event {
    private final boolean reset;
    private final Map<ResourceLocation, Advancement.Builder> added;
    private final Set<ResourceLocation> removed;
    private final Map<ResourceLocation, AdvancementProgress> progress;

    public AdvancementUpdateEvent(
            boolean reset,
            Map<ResourceLocation, Advancement.Builder> added,
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

    public Map<ResourceLocation, Advancement.Builder> getAdded() {
        return added;
    }

    public Set<ResourceLocation> getRemoved() {
        return removed;
    }

    public Map<ResourceLocation, AdvancementProgress> getProgress() {
        return progress;
    }
}
