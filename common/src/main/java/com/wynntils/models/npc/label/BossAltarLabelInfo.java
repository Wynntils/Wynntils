/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npc.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class BossAltarLabelInfo extends LabelInfo {
    private final int level;
    private final int tributeAmount;

    public BossAltarLabelInfo(
            StyledText label, String name, Location location, Entity entity, int level, int tributeAmount) {
        super(label, name, location, entity);
        this.level = level;
        this.tributeAmount = tributeAmount;
    }

    @Override
    public String toString() {
        return "BossAltarLabelInfo{" + "level="
                + level + ", tributeAmount="
                + tributeAmount + ", label="
                + label + ", name='"
                + name + '\'' + ", location="
                + location + ", entity="
                + entity + '}';
    }
}
