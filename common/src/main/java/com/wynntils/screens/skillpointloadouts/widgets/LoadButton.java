/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.skillpointloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.skillpointloadouts.SkillPointLoadoutsScreen;
import net.minecraft.network.chat.Component;

public class LoadButton extends WynntilsButton {
    private final SkillPointLoadoutsScreen parent;

    public LoadButton(int x, int y, int width, int height, Component message, SkillPointLoadoutsScreen parent) {
        super(x, y, width, height, message);
        this.parent = parent;
    }

    @Override
    public void onPress() {
        Models.SkillPoint.loadLoadout(parent.selectedLoadout.key());
    }
}
