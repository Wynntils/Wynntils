/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.dialogue;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.models.dialogue.actionbar.matchers.DialogueSegmentMatcher;
import com.wynntils.models.dialogue.actionbar.segments.DialogueSegment;
import com.wynntils.models.dialogue.event.NpcDialogueEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.List;
import java.util.Optional;
import net.neoforged.bus.api.SubscribeEvent;

public class DialogueModel extends Model {
    private DialogueSegment currentDialogueSegment;

    public DialogueModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new DialogueSegmentMatcher());
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(DialogueSegment.class, this::updateDialogue, this::endDialogue);
    }

    private void updateDialogue(DialogueSegment dialogueSegment) {
        if (isNewDialogue(dialogueSegment)) {
            startDialogue(dialogueSegment);
            return;
        }

        boolean becameReady = !currentDialogueSegment.requiresShift() && dialogueSegment.requiresShift();
        setCurrentDialogue(dialogueSegment);

        if (becameReady) {
            finishDialogue();
            return;
        }

        WynntilsMod.postEvent(new NpcDialogueEvent.Updated(
                currentDialogueSegment.getDialogueText(),
                currentDialogueSegment.requiresShift(),
                currentDialogueSegment.hasChoices()));
    }

    private void startDialogue(DialogueSegment dialogueSegment) {
        if (currentDialogueSegment != null) {
            endDialogue();
        }

        setCurrentDialogue(dialogueSegment);

        WynntilsMod.postEvent(new NpcDialogueEvent.Started(
                currentDialogueSegment.getDialogueText(),
                currentDialogueSegment.requiresShift(),
                currentDialogueSegment.hasChoices()));

        if (currentDialogueSegment.requiresShift()) {
            finishDialogue();
        }
    }

    private void finishDialogue() {
        WynntilsMod.postEvent(new NpcDialogueEvent.Finished(
                currentDialogueSegment.getDialogueText(),
                currentDialogueSegment.requiresShift(),
                currentDialogueSegment.hasChoices()));
    }

    private void endDialogue() {
        if (currentDialogueSegment == null) return;

        DialogueSegment endedDialogueSegment = currentDialogueSegment;
        currentDialogueSegment = null;

        WynntilsMod.postEvent(new NpcDialogueEvent.Ended(
                endedDialogueSegment.getDialogueText(),
                endedDialogueSegment.requiresShift(),
                endedDialogueSegment.hasChoices()));
    }

    private void setCurrentDialogue(DialogueSegment dialogueSegment) {
        currentDialogueSegment = dialogueSegment;
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        endDialogue();
    }

    private boolean isNewDialogue(DialogueSegment dialogueSegment) {
        if (currentDialogueSegment == null) return true;

        String dialogueText = dialogueSegment.getDialogueText();
        String currentDialogueText = currentDialogueSegment.getDialogueText();

        return !dialogueText.startsWith(currentDialogueText);
    }

    public boolean isDialoguePresent() {
        return currentDialogueSegment != null;
    }

    public Optional<DialogueSegment> getCurrentDialogueSegment() {
        return Optional.ofNullable(currentDialogueSegment);
    }
}
