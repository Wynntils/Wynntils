/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npc.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class FastTravelLabelInfo extends LabelInfo {
    private final String destination;

    public FastTravelLabelInfo(StyledText label, String name, Location location, Entity entity, String destination) {
        super(label, name, location, entity);
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "FastTravelLabelInfo{" + "destination='"
                + destination + '\'' + ", label="
                + label + ", name='"
                + name + '\'' + ", location="
                + location + ", entity="
                + entity + '}';
    }
}
