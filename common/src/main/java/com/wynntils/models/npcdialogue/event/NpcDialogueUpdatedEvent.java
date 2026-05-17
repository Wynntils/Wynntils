/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.event;

import net.neoforged.bus.api.Event;

public class NpcDialogueUpdatedEvent extends Event {
    private final boolean dialoguePresent;
    private final String dialogueText;
    private final boolean requiresShift;
    private final boolean hasChoices;

    public NpcDialogueUpdatedEvent(String dialogueText, boolean requiresShift, boolean hasChoices) {
        this.dialoguePresent = true;
        this.dialogueText = dialogueText;
        this.requiresShift = requiresShift;
        this.hasChoices = hasChoices;
    }

    private NpcDialogueUpdatedEvent() {
        this.dialoguePresent = false;
        this.dialogueText = "";
        this.requiresShift = false;
        this.hasChoices = false;
    }

    public static NpcDialogueUpdatedEvent dialogueGone() {
        return new NpcDialogueUpdatedEvent();
    }

    public boolean isDialoguePresent() {
        return dialoguePresent;
    }

    public String getDialogueText() {
        return dialogueText;
    }

    public boolean requiresShift() {
        return requiresShift;
    }

    public boolean hasChoices() {
        return hasChoices;
    }
}
