/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.tracker.event;

import com.wynntils.models.quests.QuestInfo;
import net.minecraftforge.eventbus.api.Event;

public class TrackerUpdatedEvent extends Event {
    private final QuestInfo questInfo;

    public TrackerUpdatedEvent(QuestInfo questInfo) {
        this.questInfo = questInfo;
    }

    public QuestInfo getQuestInfo() {
        return questInfo;
    }
}
