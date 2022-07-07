/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.mc.event.MenuEvent.MenuOpenedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wc.Model;
import com.wynntils.wc.event.WorldStateEvent;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Character implements Model {
    private CharacterInfo currentCharacter;
    private boolean inCharacterSelection;

    public boolean hasCharacter() {
        return currentCharacter != null;
    }

    public CharacterInfo getCharacterInfo() {
        return currentCharacter;
    }

    @SubscribeEvent
    public void onMenuOpened(MenuOpenedEvent e) {
        if (e.getMenuType() == MenuType.GENERIC_9x3
                && ComponentUtils.getUnformatted(e.getTitle()).equals("§8§lSelect a Class")) {
            inCharacterSelection = true;
            WynntilsMod.info("In character selection menu");
        }
    }

    @SubscribeEvent
    public void onMenuClosed(MenuClosedEvent e) {
        inCharacterSelection = false;
    }

    @SubscribeEvent
    public void onWorldStateChanged(WorldStateEvent e) {
        // Whenever we're leaving a world, clear the current character
        if (e.getOldState() == WorldState.State.WORLD) {
            currentCharacter = null;
            // This should not be needed, but have it as a safe-guard
            inCharacterSelection = false;
        }
        if (e.getNewState() == WorldState.State.CHARACTER_SELECTION) {
            WynntilsMod.info("Preparing for character selection");
        }
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent e) {
        if (inCharacterSelection) {
            currentCharacter = CharacterInfo.parseCharacter(e.getItemStack());
            WynntilsMod.info("Selected character " + currentCharacter);
        }
    }

    public static class CharacterInfo {
        private final ClassType classType;
        private final boolean reskinned;
        private final int level;
        private final UUID id;

        private CharacterInfo(ClassType classType, boolean reskinned, int level, UUID id) {
            this.classType = classType;
            this.reskinned = reskinned;
            this.level = level;
            this.id = id;
        }

        public ClassType getClassType() {
            return classType;
        }

        public boolean isReskinned() {
            return reskinned;
        }

        public int getLevel() {
            return level;
        }

        public UUID getId() {
            return id;
        }

        public static CharacterInfo parseCharacter(ItemStack itemStack) {
            List<String> lore = ItemUtils.getLore(itemStack);
            for (String s : lore) {
                // Reference.LOGGER.info("Lore: " + s);
            }

            return new CharacterInfo(null, false, 0, UUID.randomUUID());
        }

        @Override
        public String toString() {
            return "CharacterInfo[classType=" + classType + ", reskinned=" + reskinned + ", level=" + level + ", id="
                    + id + ']';
        }
    }

    public enum ClassType {
        ARCHER,
        WARRIOR,
        MAGE,
        ASSASSIN,
        SHAMAN
    }
}
