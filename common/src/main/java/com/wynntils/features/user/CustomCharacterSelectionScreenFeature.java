/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.gui.screens.CharacterSelectorScreen;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.objects.WorldState;
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
