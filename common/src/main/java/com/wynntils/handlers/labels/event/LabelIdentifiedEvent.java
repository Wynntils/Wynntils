/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.handlers.labels.type.LabelInfo;

public class LabelIdentifiedEvent extends BaseEvent {
    private final LabelInfo labelInfo;

    public LabelIdentifiedEvent(LabelInfo labelInfo) {
        this.labelInfo = labelInfo;
    }

    public LabelInfo getLabelInfo() {
        return labelInfo;
    }
}
