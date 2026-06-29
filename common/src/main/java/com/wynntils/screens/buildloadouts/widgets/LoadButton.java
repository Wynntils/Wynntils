/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
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
        Models.SkillPoint.loadLoadout(parent.selectedLoadout.key());
        Models.AbilityTree.loadAbilityTree(
                parent.selectedLoadout.key(),
                errorMsg -> {
                    // Called if the query fails or a node does not unlock
                    WynntilsMod.error("Failed to load ability tree: " + errorMsg);
                    // Optional: send a chat message or show a toast
                    // McUtils.sendMessageToClient(Component.literal("§cFailed to apply loadout: " + errorMsg));
                },
                () -> {
                    // Called when every node has been clicked and verified
                    WynntilsMod.info("Ability tree loadout applied successfully");
                    // Optional: refresh UI, play a sound, etc.
                }
        );
    }
}
