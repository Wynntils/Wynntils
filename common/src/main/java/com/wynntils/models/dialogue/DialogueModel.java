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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.neoforged.bus.api.SubscribeEvent;

public class DialogueModel extends Model {
    private final Set<Class<? extends ActionBarSegment>> hiddenSegments = new HashSet<>();

    private DialogueSegment currentDialogueSegment;

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

        boolean wasDoneRendering = currentDialogueSegment.getDialogueText().equals(dialogueSegment.getDialogueText());
        setCurrentDialogue(dialogueSegment);

        if (wasDoneRendering) {
            WynntilsMod.postEvent(new NpcDialogueEvent.Finished(
                    currentDialogueSegment.getDialogueText(),
                    currentDialogueSegment.requiresShift(),
                    currentDialogueSegment.hasChoices()));
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

    private boolean isNewDialogue(DialogueSegment dialogueSegment) {
        if (currentDialogueSegment == null) return true;

        String dialogueText = dialogueSegment.getDialogueText();
        String currentDialogueText = currentDialogueSegment.getDialogueText();

        // On purpose doing !() so if is equals, don't check startsWith, performance optimization
        return !(dialogueText.equals(currentDialogueText) || dialogueText.startsWith(currentDialogueText));
    }

    public StyledText getCurrentDialogue() {
        return currentDialogueSegment.getContent();
    }

    public boolean isDialoguePresent() {
        return currentDialogueSegment != null;
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
