/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class WorldEventNameInfo extends LabelInfo {
    private final String worldEventName;

    protected WorldEventNameInfo(StyledText label, Location location, Entity entity, String worldEventName) {
        super(label, location, entity);

        this.worldEventName = worldEventName;
    }

    public String getWorldEventName() {
        return worldEventName;
    }

    @Override
    public String toString() {
        return "WorldEventNameInfo{" + "worldEventName='" + worldEventName + '\'' + '}';
    }
}
