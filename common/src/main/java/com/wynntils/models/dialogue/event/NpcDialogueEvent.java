/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.dialogue.event;

import net.neoforged.bus.api.Event;

public abstract class NpcDialogueEvent extends Event {
    private final String dialogueText;
    private final boolean requiresShift;
    private final boolean hasChoices;

    protected NpcDialogueEvent(String dialogueText, boolean requiresShift, boolean hasChoices) {
        this.dialogueText = dialogueText;
        this.requiresShift = requiresShift;
        this.hasChoices = hasChoices;
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

    public static class Updated extends NpcDialogueEvent {
        public Updated(String dialogueText, boolean requiresShift, boolean hasChoices) {
            super(dialogueText, requiresShift, hasChoices);
        }
    }

    public static class Started extends NpcDialogueEvent {
        public Started(String dialogueText, boolean requiresShift, boolean hasChoices) {
            super(dialogueText, requiresShift, hasChoices);
        }
    }

    public static class Finished extends NpcDialogueEvent {
        public Finished(String dialogueText, boolean requiresShift, boolean hasChoices) {
            super(dialogueText, requiresShift, hasChoices);
        }
    }

    public static class Ended extends NpcDialogueEvent {
        public Ended(String dialogueText, boolean requiresShift, boolean hasChoices) {
            super(dialogueText, requiresShift, hasChoices);
        }
    }
}
