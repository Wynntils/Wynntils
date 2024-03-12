/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * Represents the current dialogue of an NPC.
 *
 * @param currentDialogue The current dialogue of the NPC.
 * @param dialogueType    The type of dialogue.
 * @param isProtected     Whether the dialogue is protects the player from being attacked.
 * @param addTime         The time the dialogue was added.
 * @param removeTime      The time the dialogue will be removed. This is used for confirmationless dialogues.
 */
public record NpcDialogue(
        List<StyledText> currentDialogue,
        NpcDialogueType dialogueType,
        boolean isProtected,
        long addTime,
        long removeTime) {
    public static final NpcDialogue EMPTY = new NpcDialogue(List.of(), NpcDialogueType.NONE, false, 0, 0);

    public boolean isEmpty() {
        return currentDialogue.isEmpty();
    }

    public List<Component> dialogueComponent() {
        return currentDialogue.stream()
                .map(StyledText::getComponent)
                .map(c -> (Component) c)
                .toList();
    }
}
