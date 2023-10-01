/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.label.infos;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.label.type.LabelInfo;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.type.Location;

public abstract class ProfessionLabelInfo extends LabelInfo {
    protected final ProfessionType professionType;

    protected ProfessionLabelInfo(StyledText label, String name, Location location, ProfessionType professionType) {
        super(label, name, location);
        this.professionType = professionType;
    }

    public ProfessionType getProfessionType() {
        return professionType;
    }
}
