/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npc.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class NpcLabelInfo extends LabelInfo {
    public NpcLabelInfo(StyledText label, Location location, Entity entity) {
        super(label, location, entity);
    }

    public NpcLabelInfo(StyledText label, String name, Location location, Entity entity) {
        super(label, name, location, entity);
    }

    @Override
    public String toString() {
        return "NpcLabelInfo{" + "label="
                + label + ", name='"
                + name + '\'' + ", location="
                + location + ", entity="
                + entity + '}';
    }
}
