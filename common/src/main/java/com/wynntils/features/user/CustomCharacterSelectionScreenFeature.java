/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.screens.characterselector.CharacterSelectorScreen;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CustomCharacterSelectionScreenFeature extends UserFeature {
    @Config
    public boolean onlyOpenOnce = false;

    private boolean openedInThisCharacterSelectionState = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenOpen(ScreenOpenedEvent event) {
        if ((onlyOpenOnce && openedInThisCharacterSelectionState)
                || Managers.WorldState.getCurrentState() != WorldState.CHARACTER_SELECTION) return;

        if (!ComponentUtils.getCoded(event.getScreen().getTitle()).equals("§8§lSelect a Character")) {
            return;
        }

        openedInThisCharacterSelectionState = true;

        McUtils.mc().setScreen(CharacterSelectorScreen.create());
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.CHARACTER_SELECTION) {
            openedInThisCharacterSelectionState = false;
        }
    }
}
