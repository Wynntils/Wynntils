/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.quests.event;

import com.wynntils.core.events.WynntilsEvent;
import com.wynntils.wynn.model.quests.QuestInfo;

public class TrackedQuestUpdateEvent extends WynntilsEvent {
    private final QuestInfo questInfo;

    public TrackedQuestUpdateEvent(QuestInfo questInfo) {
        this.questInfo = questInfo;
    }

    public QuestInfo getQuestInfo() {
        return questInfo;
    }
}
