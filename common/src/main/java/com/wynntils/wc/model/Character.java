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
import com.wynntils.wc.objects.ClassType;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Character implements Model {

    private static final Pattern CLASS_PATTERN = Pattern.compile("§e- §r§7Class: §r§f(.+)");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("§e- §r§7Level: §r§f(\\d+)");

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
            if (e.getItemStack().getItem() == Items.AIR) return;
            currentCharacter = CharacterInfo.parseCharacter(e.getItemStack(), e.getSlotNum());
            WynntilsMod.info("Selected character " + currentCharacter);
        }
    }

    public static class CharacterInfo {
        private final ClassType classType;
        private final boolean reskinned;
        private final int level;

        // This field is basically the slot id of the class,
        // meaning that if a class changes slots, the ID will not be persistent.
        // This was implemented the same way by legacy.
        private final int id;

        private CharacterInfo(ClassType classType, boolean reskinned, int level, int id) {
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

        public int getId() {
            return id;
        }

        public static CharacterInfo parseCharacter(ItemStack itemStack, int slotNum) {
            List<String> lore = ItemUtils.getLore(itemStack);

            int level = 0;
            ClassType classType = null;

            for (String line : lore) {
                Matcher matcher = LEVEL_PATTERN.matcher(line);
                if (matcher.matches()) {
                    level = Integer.parseInt(matcher.group(1));
                    continue;
                }

                matcher = CLASS_PATTERN.matcher(line);

                if (matcher.matches()) {
                    classType = ClassType.fromName(matcher.group(1));
                }
            }

            return new CharacterInfo(
                    classType, classType != null && ClassType.isReskinned(classType.getName()), level, slotNum);
        }

        @Override
        public String toString() {
            return "CharacterInfo[classType=" + classType + ", reskinned=" + reskinned + ", level=" + level + ", id="
                    + id + ']';
        }
    }
}
