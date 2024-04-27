/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public abstract class ProfessionLabelInfo extends LabelInfo {
    protected final ProfessionType professionType;

    protected ProfessionLabelInfo(
            StyledText label, String name, Location location, Entity entity, ProfessionType professionType) {
        super(label, name, location, entity);
        this.professionType = professionType;
    }

    public ProfessionType getProfessionType() {
        return professionType;
    }
}
