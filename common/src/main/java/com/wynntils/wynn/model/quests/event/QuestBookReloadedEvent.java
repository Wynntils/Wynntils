/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.quests.event;

import com.wynntils.core.events.WynntilsEvent;

public abstract class QuestBookReloadedEvent extends WynntilsEvent {
    public static class QuestsReloaded extends QuestBookReloadedEvent {}

    public static class MiniQuestsReloaded extends QuestBookReloadedEvent {}

    public static class DialogueHistoryReloaded extends QuestBookReloadedEvent {}
}
