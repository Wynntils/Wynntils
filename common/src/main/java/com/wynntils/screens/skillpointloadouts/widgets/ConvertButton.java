/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.skillpointloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.skillpointloadouts.SkillPointLoadoutsScreen;
import com.wynntils.utils.type.Pair;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class ConvertButton extends WynntilsButton {
    private final SkillPointLoadoutsScreen parent;

    public ConvertButton(int x, int y, int width, int height, Component message, SkillPointLoadoutsScreen parent) {
        super(x, y, width, height, message);
        this.parent = parent;
        this.setTooltip(Tooltip.create(Component.translatable("screens.wynntils.skillPointLoadouts.convertTooltip")));
    }

    @Override
    public void onPress() {
        if (parent.selectedLoadout.value().isBuild()) {
            Models.SkillPoint.saveSkillPoints(
                    parent.selectedLoadout.key(), parent.selectedLoadout.value().getSkillPointsAsArray());
        } else {
            Models.SkillPoint.saveBuild(
                    parent.selectedLoadout.key(), parent.selectedLoadout.value().getSkillPointsAsArray());
        }
        parent.populateLoadouts();
        parent.setSelectedLoadout(new Pair<>(
                parent.selectedLoadout.key(), Models.SkillPoint.getLoadouts().get(parent.selectedLoadout.key())));
    }
}
