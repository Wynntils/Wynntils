/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests.type;

import com.wynntils.models.content.type.ContentStatus;
import com.wynntils.screens.questbook.WynntilsQuestBookScreen;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum QuestStatus {
    STARTED(Component.literal("Started...").withStyle(ChatFormatting.YELLOW)),
    CAN_START(Component.literal("Can start...").withStyle(ChatFormatting.YELLOW)),
    CANNOT_START(Component.literal("Cannot start...").withStyle(ChatFormatting.RED)),
    COMPLETED(Component.literal("Completed!").withStyle(ChatFormatting.GREEN));

    /** This component is used to reconstruct quest tooltip in {@link WynntilsQuestBookScreen}.
     */
    private final Component questBookComponent;

    QuestStatus(Component component) {
        this.questBookComponent = component;
    }

    public static QuestStatus fromContentStatus(ContentStatus contentStatus) {
        return switch (contentStatus) {
            case STARTED -> STARTED;
            case AVAILABLE -> CAN_START;
            case UNAVAILABLE -> CANNOT_START;
            case COMPLETED -> COMPLETED;
        };
    }

    public Component getQuestBookComponent() {
        return questBookComponent;
    }

    public static QuestStatus fromString(String str) {
        try {
            return QuestStatus.valueOf(str.toUpperCase(Locale.ROOT).replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Use CANNOT_START as fallback... it's as good as any
            return CANNOT_START;
        }
    }
}
