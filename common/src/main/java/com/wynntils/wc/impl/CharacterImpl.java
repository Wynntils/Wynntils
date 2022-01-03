/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.impl;

import com.wynntils.mc.event.MenuOpenedEvent;
import com.wynntils.utils.Utils;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.model.Character;
import com.wynntils.wc.model.WorldState.State;
import java.util.UUID;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CharacterImpl implements Character {
    private CharacterInfo currentCharacter;

    @Override
    public boolean hasCharacter() {
        return currentCharacter != null;
    }

    @Override
    public CharacterInfo getCharacterInfo() {
        return currentCharacter;
    }

    @SubscribeEvent
    public void onMenuOpened(MenuOpenedEvent e) {
        if (e.getMenuType().equals(MenuOpenedEvent.MENU_3_LINES)
                && Utils.getUnformatted(e.getTitle()).equals("§8§lSelect a Class")) {
            System.out.println("In character selection menu");
        }
    }

    @SubscribeEvent
    public void onWorldStateChanged(WorldStateEvent e) {
        // Whenever we're leaving a world, clear the current character
        if (e.getOldState() == State.WORLD) {
            currentCharacter = null;
        }
        if (e.getNewState() == State.CHARACTER_SELECTION) {
            System.out.println("Preparing for character selection");
        }
    }
}
