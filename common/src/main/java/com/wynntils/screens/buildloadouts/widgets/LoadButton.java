/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.Loadout;
import com.wynntils.screens.buildloadouts.type.LoadoutType;
import com.wynntils.utils.colors.CommonColors;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class LoadButton extends WynntilsButton {
    private final BuildLoadoutsScreen parent;

    public LoadButton(int x, int y, int width, int height, Component message, BuildLoadoutsScreen parent) {
        super(x, y, width, height, message);
        this.parent = parent;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        Loadout loadout = parent.getSelectedLoadout();

        if (loadout.type() == LoadoutType.SKILL_POINT) {
            parent.setStatus("Loading skill points...", CommonColors.YELLOW);
            Models.SkillPoint.loadLoadout(
                    loadout.name(),
                    error -> parent.setStatus(error, CommonColors.RED),
                    () -> parent.setStatus("Skill points loaded successfully!", CommonColors.GREEN));
            return;
        }

        if (loadout.type() == LoadoutType.ABILITY_TREE) {
            parent.setStatus("Applying ability tree...", CommonColors.YELLOW);
            Models.AbilityTree.loadAbilityTree(
                    loadout.name(),
                    status -> parent.setStatus(status, CommonColors.YELLOW),
                    error -> parent.setStatus(error, CommonColors.RED),
                    completed -> parent.setStatus(completed, CommonColors.GREEN));
            return;
        }

        if (loadout.type() == LoadoutType.ASPECT) {
            parent.setStatus("Applying aspects...", CommonColors.YELLOW);
            Models.Aspect.loadAspectLoadout(
                    loadout.name(),
                    status -> parent.setStatus(status, CommonColors.YELLOW),
                    error -> parent.setStatus(error, CommonColors.RED),
                    completed -> parent.setStatus(completed, CommonColors.GREEN));
            return;
        }

        if (loadout.type() == LoadoutType.BUILD) {
            parent.setStatus("Loading skill points...", CommonColors.YELLOW);
            Models.SkillPoint.loadLoadout(
                    loadout.name(),
                    error -> parent.setStatus(error, CommonColors.RED),
                    () -> {
                        if (loadout.hasAbilityTree()) {
                            parent.setStatus("Skill points loaded. Applying ability tree...", CommonColors.YELLOW);
                            Models.AbilityTree.loadAbilityTree(
                                    loadout.name(),
                                    status -> parent.setStatus(status, CommonColors.YELLOW),
                                    error -> parent.setStatus(error, CommonColors.RED),
                                    completed -> {
                                        if (loadout.hasAspects()) {
                                            parent.setStatus("Ability tree applied. Applying aspects...", CommonColors.YELLOW);
                                            Models.Aspect.loadAspectLoadout(
                                                    loadout.name(),
                                                    status -> parent.setStatus(status, CommonColors.YELLOW),
                                                    error -> parent.setStatus(error, CommonColors.RED),
                                                    done -> parent.setStatus(done, CommonColors.GREEN));
                                        } else {
                                            parent.setStatus(completed, CommonColors.GREEN);
                                        }
                                    });
                        } else if (loadout.hasAspects()) {
                            parent.setStatus("Skill points loaded. Applying aspects...", CommonColors.YELLOW);
                            Models.Aspect.loadAspectLoadout(
                                    loadout.name(),
                                    status -> parent.setStatus(status, CommonColors.YELLOW),
                                    error -> parent.setStatus(error, CommonColors.RED),
                                    completed -> parent.setStatus(completed, CommonColors.GREEN));
                        } else {
                            parent.setStatus("Skill points loaded successfully!", CommonColors.GREEN);
                        }
                    });
        }
    }
}
