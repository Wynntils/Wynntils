/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.impl;

import com.wynntils.core.Reference;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.mc.event.MenuEvent.MenuOpenedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.model.Character;
import com.wynntils.wc.model.WorldState.State;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;

public class CharacterImpl implements Character {
    private CharacterInfo currentCharacter;
    private boolean inCharacterSelection;

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
        if (e.getMenuType() == MenuType.GENERIC_9x3
                && ComponentUtils.getUnformatted(e.getTitle()).equals("§8§lSelect a Class")) {
            inCharacterSelection = true;
            Reference.LOGGER.info("In character selection menu");
        }
    }

    @SubscribeEvent
    public void onMenuClosed(MenuClosedEvent e) {
        inCharacterSelection = false;
    }

    @SubscribeEvent
    public void onWorldStateChanged(WorldStateEvent e) {
        // Whenever we're leaving a world, clear the current character
        if (e.getOldState() == State.WORLD) {
            currentCharacter = null;
            // This should not be needed, but have it as a safe-guard
            inCharacterSelection = false;
        }
        if (e.getNewState() == State.CHARACTER_SELECTION) {
            Reference.LOGGER.info("Preparing for character selection");
        }
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent e) {
        if (inCharacterSelection) {
            currentCharacter = CharacterInfoImpl.parseCharacter(e.getItemStack());
            Reference.LOGGER.info("Selected character " + currentCharacter);
        }
    }

    public static class CharacterInfoImpl implements CharacterInfo {
        private final ClassType classType;
        private final boolean reskinned;
        private final int level;
        private final UUID id;

        private CharacterInfoImpl(ClassType classType, boolean reskinned, int level, UUID id) {
            this.classType = classType;
            this.reskinned = reskinned;
            this.level = level;
            this.id = id;
        }

        @Override
        public ClassType getClassType() {
            return classType;
        }

        @Override
        public boolean isReskinned() {
            return reskinned;
        }

        @Override
        public int getLevel() {
            return level;
        }

        @Override
        public UUID getId() {
            return id;
        }

        public static CharacterInfo parseCharacter(ItemStack itemStack) {
            List<String> lore = ItemUtils.getLore(itemStack);
            for (String s : lore) {
                // Reference.LOGGER.info("Lore: " + s);
            }

            return new CharacterInfoImpl(null, false, 0, UUID.randomUUID());
        }

        @Override
        public String toString() {
            return "CharacterInfoImpl[classType=" + classType + ", reskinned=" + reskinned + ", level=" + level
                    + ", id=" + id + ']';
        }
    }
}
