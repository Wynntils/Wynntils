/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.core.text.StyledText2;

public class MiscItem extends GameItem {
    private final StyledText2 name;
    private final boolean untradable;
    private final boolean questItem;

    public MiscItem(StyledText2 name, boolean untradable, boolean questItem) {
        this.name = name;
        this.untradable = untradable;
        this.questItem = questItem;
    }

    public StyledText2 getName() {
        return name;
    }

    public boolean isUntradable() {
        return untradable;
    }

    public boolean isQuestItem() {
        return questItem;
    }

    @Override
    public String toString() {
        return "MiscItem{" + "untradable=" + untradable + ", questItem=" + questItem + '}';
    }
}
