/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.Time;
import net.minecraft.world.entity.Entity;

public class WorldEventCountdownInfo extends LabelInfo {
    private final Time startTime;

    protected WorldEventCountdownInfo(StyledText label, Location location, Entity entity, Time startTime) {
        super(label, location, entity);

        this.startTime = startTime;
    }

    public Time getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "WorldEventCountdownInfo{" + "startTime="
                + startTime + ", label="
                + label + ", name='"
                + name + '\'' + ", location="
                + location + ", entity="
                + entity + '}';
    }
}
