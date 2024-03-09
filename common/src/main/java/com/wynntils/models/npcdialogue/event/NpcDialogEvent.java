/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.event;

import com.wynntils.models.npcdialogue.type.NpcDialogue;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class NpcDialogEvent extends Event {
    private final NpcDialogue dialogue;

    public NpcDialogEvent(NpcDialogue dialogue) {
        this.dialogue = dialogue;
    }

    public NpcDialogue getDialogue() {
        return dialogue;
    }
}
