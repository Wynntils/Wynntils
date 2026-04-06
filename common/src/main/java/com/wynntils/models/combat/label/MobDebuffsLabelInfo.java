/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import java.util.Map;
import net.minecraft.world.entity.Entity;

public class MobDebuffsLabelInfo extends LabelInfo {
    private final Map<DebuffType, Integer> debuffs;

    public MobDebuffsLabelInfo(StyledText label, Location location, Entity entity, Map<DebuffType, Integer> debuffs) {
        super(label, location, entity);
        this.debuffs = debuffs;
    }

    public Map<DebuffType, Integer> getDebuffs() {
        return debuffs;
    }
}
