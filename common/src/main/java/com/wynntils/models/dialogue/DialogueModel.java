/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.dialogue;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.models.dialogue.actionbar.matchers.DialogueFadeSegmentMatcher;
import com.wynntils.models.dialogue.actionbar.matchers.DialogueSegmentMatcher;
import com.wynntils.models.dialogue.actionbar.segments.DialogueSegment;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.neoforged.bus.api.SubscribeEvent;

public class DialogueModel extends Model {
    private final Set<Class<? extends ActionBarSegment>> hiddenSegments = new HashSet<>();

    private StyledText currentDialogue;

    public DialogueModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new DialogueSegmentMatcher());
        Handlers.ActionBar.registerSegment(new DialogueFadeSegmentMatcher());
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(DialogueSegment.class, this::updateDialogue, this::clearDialogue);
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        hiddenSegments.forEach(segment -> event.setSegmentEnabled(segment, false));
    }

    private void clearDialogue() {
        currentDialogue = null;
    }

    private void updateDialogue(DialogueSegment dialogueSegment) {
        currentDialogue = dialogueSegment.getContent();
    }

    public StyledText getCurrentDialogue() {
        return currentDialogue;
    }

    public boolean isDialoguePresent() {
        return currentDialogue != null;
    }

    public void setHideSegments(boolean enabled, boolean dialogue, boolean fade) {
        if (enabled) {
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
