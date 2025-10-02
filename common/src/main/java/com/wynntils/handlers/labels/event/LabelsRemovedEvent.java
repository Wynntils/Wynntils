/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.handlers.labels.type.LabelInfo;
import java.util.Collections;
import java.util.List;

public class LabelsRemovedEvent extends BaseEvent {
    private final List<LabelInfo> removedLabels;

    public LabelsRemovedEvent(List<LabelInfo> removedLabels) {
        this.removedLabels = Collections.unmodifiableList(removedLabels);
    }

    public List<LabelInfo> getRemovedLabels() {
        return removedLabels;
    }
}
