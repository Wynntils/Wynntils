/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.skillpointloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.skillpointloadouts.SkillPointLoadoutsScreen;
import net.minecraft.network.chat.Component;

public class DeleteButton extends WynntilsButton {
    private final SkillPointLoadoutsScreen parent;

    public DeleteButton(int x, int y, int width, int height, Component message, SkillPointLoadoutsScreen parent) {
        super(x, y, width, height, message);
        this.parent = parent;
    }

    @Override
    public void onPress() {
        Models.SkillPoint.deleteLoadout(parent.selectedLoadout.key());
        parent.setSelectedLoadout(null);
        parent.populateLoadouts();
        parent.doScroll(0); // force scrollPercent refresh
    }
}
