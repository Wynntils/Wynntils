/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.Loadout;
import com.wynntils.screens.buildloadouts.type.LoadoutType;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class ConvertButton extends WynntilsButton {
    private final BuildLoadoutsScreen parent;

    public ConvertButton(int x, int y, int width, int height, Component message, BuildLoadoutsScreen parent) {
        super(x, y, width, height, message);
        this.parent = parent;
        this.setTooltip(Tooltip.create(Component.translatable("screens.wynntils.buildLoadouts.convertTooltip")));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        Loadout loadout = parent.getSelectedLoadout();
        if (loadout.type() == LoadoutType.BUILD) {
            Models.SkillPoint.saveSkillPoints(
                    loadout.name(), loadout.skillPoints().getSkillPointsAsArray());
        } else {
            Models.SkillPoint.saveBuild(loadout.name(), loadout.skillPoints().getSkillPointsAsArray());
        }
        parent.populateLoadouts();
        parent.setSelectedLoadout(parent.getLoadout(loadout.name()));
    }
}
