/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import java.util.List;

public record NpcDialogue(List<StyledText> currentDialogue, NpcDialogueType dialogueType, boolean isProtected) {
    public static final NpcDialogue EMPTY = new NpcDialogue(List.of(), NpcDialogueType.NONE, false);

    public boolean isEmpty() {
        return currentDialogue.isEmpty();
    }
}
