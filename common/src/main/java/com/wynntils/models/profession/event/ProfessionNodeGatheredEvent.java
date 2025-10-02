/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.event;

import com.wynntils.core.events.BaseEvent;

public abstract class ProfessionNodeGatheredEvent extends BaseEvent {
    public static class LabelShown extends ProfessionNodeGatheredEvent {}
}
