/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wrappedscreens.activities;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;

public class WynntilsQuestBookHolder extends AbstractActivityHolder<WynntilsQuestBookScreen, QuestInfo> {
    @Override
    protected WynntilsQuestBookScreen createWrappedScreen(WrappedScreenInfo wrappedScreenInfo) {
        return new WynntilsQuestBookScreen(wrappedScreenInfo, this);
    }

    @Override
    protected QuestInfo itemToInfo(ActivityInfo activityInfo) {
        return Models.Quest.getQuestInfoFromActivity(activityInfo);
    }

    public static void openScreen() {
        ContainerUtils.openInventory(InventoryUtils.CONTENT_BOOK_SLOT_NUM);
    }
}
