/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.type;

import com.google.common.collect.ComparisonChain;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public abstract class LabelInfo implements Comparable<LabelInfo> {
    protected final StyledText label;
    protected final String name;
    protected final Location location;

    // Must be transient, we don't want to serialize this to JSON
    protected final transient Entity entity;

    protected LabelInfo(StyledText label, Location location, Entity entity) {
        this.label = label;
        this.name = label.getStringWithoutFormatting();
        this.location = location;
        this.entity = entity;
    }

    protected LabelInfo(StyledText label, String name, Location location, Entity entity) {
        this.label = label;
        this.name = name;
        this.location = location;
        this.entity = entity;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public int compareTo(LabelInfo other) {
        return ComparisonChain.start()
                .compare(name, other.name)
                .compare(location, other.location)
                .result();
    }
}
