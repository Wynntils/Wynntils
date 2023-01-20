/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class QuestBookReloadedEvent extends Event {
    public static class QuestsReloaded extends QuestBookReloadedEvent {}

    public static class MiniQuestsReloaded extends QuestBookReloadedEvent {}

    public static class DialogueHistoryReloaded extends QuestBookReloadedEvent {}
}
