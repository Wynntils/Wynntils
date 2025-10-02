/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.models.npcdialogue.type.NpcDialogue;

/**
 * Note that currently this event is only fired for non-confirmationless dialogues.
 */
public class NpcDialogueRemoved extends BaseEvent {
    private final NpcDialogue removedDialogue;

    public NpcDialogueRemoved(NpcDialogue removedDialogue) {
        this.removedDialogue = removedDialogue;
    }

    public NpcDialogue getRemovedDialogue() {
        return removedDialogue;
    }
}
