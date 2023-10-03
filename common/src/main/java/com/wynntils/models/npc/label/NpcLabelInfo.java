/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npc.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;

public class NpcLabelInfo extends LabelInfo {
    public NpcLabelInfo(StyledText label, Location location) {
        super(label, location);
    }

    public NpcLabelInfo(StyledText label, String name, Location location) {
        super(label, name, location);
    }

    @Override
    public String toString() {
        return "NpcLabelInfo{" + "label=" + label + ", labelString='" + name + '\'' + ", location=" + location + '}';
    }
}
