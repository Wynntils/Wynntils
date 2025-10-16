/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import com.wynntils.mc.event.MobEffectEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.world.effect.MobEffects;

public final class ChatPageProcessor {
    // Test in ChatPageProcessor_NPC_CONFIRM_PATTERN
    private static final Pattern NPC_CONFIRM_PATTERN =
            Pattern.compile("^ *§[47]Press §[cf](SNEAK|SHIFT) §[47]to continue$");
    // Test in ChatPageProcessor_NPC_SELECT_PATTERN
    private static final Pattern NPC_SELECT_PATTERN =
            Pattern.compile("^ *§[47cf](Select|CLICK) §[47cf]an option (§[47])?to continue$");
    private static final Pattern EMPTY_LINE_PATTERN = Pattern.compile("^\\s*(§r|À+)?\\s*$");

    private boolean isProtected = false;

    public void onStatusEffectUpdate(MobEffectEvent.Update event) {
        if (event.getEntity() != McUtils.player()) return;

        if (event.getEffect().equals(MobEffects.MOVEMENT_SLOWDOWN.value())
                && event.getEffectAmplifier() == 3
                && event.getEffectDurationTicks() == 32767) {
            isProtected = true;
        }
    }

    public void onStatusEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEntity() != McUtils.player()) return;

        if (event.getEffect().equals(MobEffects.MOVEMENT_SLOWDOWN.value())) {
            isProtected = false;
        }
    }

    public void handlePage(List<StyledText> page) {
        // First we trim the empty lines at the start and end
        NpcDialogueType type = NpcDialogueType.NONE;
        ArrayList<StyledText> pageContent = new ArrayList<>(page);

        if (!pageContent.isEmpty()) {
            while (pageContent.getFirst().isBlank()) {
                pageContent.removeFirst();
            }
            while (pageContent.getLast().isBlank()) {
                pageContent.removeLast();
            }
            // Then we check for markers saying if this is a selection dialogue or
            // a confirmation dialogue
            StyledText lastLine = pageContent.getLast();
            if (lastLine.find(NPC_SELECT_PATTERN)) {
                type = NpcDialogueType.SELECTION;
            } else if (lastLine.find(NPC_CONFIRM_PATTERN)) {
                type = NpcDialogueType.NORMAL;
            } else {
                // This could be a "clear screen" with no real message in it
                while (!pageContent.isEmpty()
                        && (pageContent.getFirst().find(EMPTY_LINE_PATTERN)
                                || pageContent.getFirst().isBlank())) {
                    pageContent.removeFirst();
                }
                if (pageContent.isEmpty()) {
                    type = NpcDialogueType.NONE;
                } else {
                    type = NpcDialogueType.CONFIRMATIONLESS;
                }
            }

            if (type == NpcDialogueType.SELECTION || type == NpcDialogueType.NORMAL) {
                // Remove the last line, and the empty line before it
                pageContent.removeLast();
                if (!pageContent.isEmpty() && pageContent.getLast().isBlank()) {
                    pageContent.removeLast();
                } else {
                    WynntilsMod.warn("Malformed dialog [#3]: " + lastLine);
                }
            }
        }
        Models.NpcDialogue.handleDialogue(pageContent, isProtected, type);
    }

    public void reset() {
        // Reset chat handler
        isProtected = false;
    }
}
