/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.dialogue;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.models.dialogue.actionbar.matchers.DialogueFadeSegmentMatcher;
import com.wynntils.models.dialogue.actionbar.matchers.DialogueSegmentMatcher;
import com.wynntils.models.dialogue.actionbar.segments.DialogueSegment;
import com.wynntils.models.dialogue.event.NpcDialogueEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DialogueModel extends Model {
    private final Set<Class<? extends ActionBarSegment>> hiddenSegments = new HashSet<>();

    private StyledText currentDialogue;
    private boolean dialogueActive = false;
    private String currentDialogueText = "";
    private boolean currentDialogueRequiresShift = false;
    private boolean currentDialogueHasChoices = false;

    public DialogueModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new DialogueSegmentMatcher());
        Handlers.ActionBar.registerSegment(new DialogueFadeSegmentMatcher());
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(DialogueSegment.class, this::updateDialogue, this::endDialogue);
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        hiddenSegments.forEach(segment -> event.setSegmentEnabled(segment, false));
    }

    private void updateDialogue(DialogueSegment dialogueSegment) {
        if (isNewDialogue(dialogueSegment)) {
            startDialogue(dialogueSegment);
            return;
        }

        boolean wasDoneRendering = currentDialogueText.equals(dialogueSegment.getDialogueText());
        setCurrentDialogue(dialogueSegment);

        if (!wasDoneRendering && currentDialogueRequiresShift) {
            WynntilsMod.postEvent(new NpcDialogueEvent.Finished(
                    currentDialogueText, currentDialogueRequiresShift, currentDialogueHasChoices));
            return;
        }

        WynntilsMod.postEvent(new NpcDialogueEvent.Updated(
                currentDialogueText, currentDialogueRequiresShift, currentDialogueHasChoices));
    }

    private void startDialogue(DialogueSegment dialogueSegment) {
        if (dialogueActive) {
            endDialogue();
        }

        setCurrentDialogue(dialogueSegment);
        dialogueActive = true;

        WynntilsMod.postEvent(new NpcDialogueEvent.Started(
                currentDialogueText, currentDialogueRequiresShift, currentDialogueHasChoices));

        if (!currentDialogueHasChoices && currentDialogueRequiresShift) {
            WynntilsMod.postEvent(new NpcDialogueEvent.Finished(
                    currentDialogueText, currentDialogueRequiresShift, currentDialogueHasChoices));
        }
    }

    private void endDialogue() {
        if (!dialogueActive) return;

        String endedDialogueText = currentDialogueText;
        boolean endedDialogueRequiresShift = currentDialogueRequiresShift;
        boolean endedDialogueHasChoices = currentDialogueHasChoices;
        dialogueActive = false;
        currentDialogue = null;
        currentDialogueText = "";
        currentDialogueRequiresShift = false;
        currentDialogueHasChoices = false;

        WynntilsMod.postEvent(
                new NpcDialogueEvent.Ended(endedDialogueText, endedDialogueRequiresShift, endedDialogueHasChoices));
    }

    private void setCurrentDialogue(DialogueSegment dialogueSegment) {
        currentDialogue = dialogueSegment.getContent();
        currentDialogueText = dialogueSegment.getDialogueText();
        currentDialogueRequiresShift = dialogueSegment.requiresShift();
        currentDialogueHasChoices = dialogueSegment.hasChoices();
    }

    private boolean isNewDialogue(DialogueSegment dialogueSegment) {
        if (!dialogueActive) return true;

        String dialogueText = dialogueSegment.getDialogueText();
        if (!currentDialogueRequiresShift) return false;
        if (dialogueText.equals(currentDialogueText) || dialogueText.startsWith(currentDialogueText)) return false;

        return !currentDialogueHasChoices && !dialogueSegment.hasChoices();
    }

    public StyledText getCurrentDialogue() {
        return currentDialogue;
    }

    public boolean isDialoguePresent() {
        return currentDialogue != null;
    }

    public void setHideSegments(boolean disabled, boolean dialogue, boolean fade) {
        if (disabled) {
            hiddenSegments.remove(DialogueSegment.class);
            hiddenSegments.remove(DialogueSegment.DialogueFadeSegment.class);
            return;
        }
        if (dialogue) {
            hiddenSegments.add(DialogueSegment.class);
        } else {
            hiddenSegments.remove(DialogueSegment.class);
        }

        if (fade) {
            hiddenSegments.add(DialogueSegment.DialogueFadeSegment.class);
        } else {
            hiddenSegments.remove(DialogueSegment.DialogueFadeSegment.class);
        }
    }
}
