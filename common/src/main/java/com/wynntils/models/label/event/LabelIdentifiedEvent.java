/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.label.event;

import com.wynntils.models.label.type.LabelInfo;
import net.minecraftforge.eventbus.api.Event;

public class LabelIdentifiedEvent extends Event {
    private final LabelInfo labelInfo;

    public LabelIdentifiedEvent(LabelInfo labelInfo) {
        this.labelInfo = labelInfo;
    }

    public LabelInfo getLabelInfo() {
        return labelInfo;
    }
}
