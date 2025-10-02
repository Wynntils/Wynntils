/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;

public class CutsceneStartedEvent extends BaseEvent implements OperationCancelable {
    private final boolean groupCutscene;

    public CutsceneStartedEvent(boolean groupCutscene) {
        this.groupCutscene = groupCutscene;
    }

    public boolean isGroupCutscene() {
        return groupCutscene;
    }
}
