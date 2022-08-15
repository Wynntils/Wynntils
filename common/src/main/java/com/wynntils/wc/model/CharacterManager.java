/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.mc.event.MenuEvent.MenuOpenedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.objects.ClassType;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CharacterManager extends CoreManager {
    private static final Pattern CLASS_PATTERN = Pattern.compile("§e- §r§7Class: §r§f(.+)");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("§e- §r§7Level: §r§f(\\d+)");

    /* These values are copied from a post by Salted, https://forums.wynncraft.com/threads/new-levels-xp-requirement.261763/
     * which points to the data at https://pastebin.com/fCTfEkaC
     * Note that the last value is the sum of all preceeding values
     */
    private static final int[] LEVEL_UP_XP_REQUIREMENTS = {
        110, 190, 275, 385, 505, 645, 790, 940, 1100, 1370, 1570, 1800, 2090, 2400, 2720, 3100, 3600, 4150, 4800, 5300,
        5900, 6750, 7750, 8900, 10200, 11650, 13300, 15200, 17150, 19600, 22100, 24900, 28000, 31500, 35500, 39900,
        44700, 50000, 55800, 62000, 68800, 76400, 84700, 93800, 103800, 114800, 126800, 140000, 154500, 170300, 187600,
        206500, 227000, 249500, 274000, 300500, 329500, 361000, 395000, 432200, 472300, 515800, 562800, 613700, 668600,
        728000, 792000, 860000, 935000, 1040400, 1154400, 1282600, 1414800, 1567500, 1730400, 1837000, 1954800, 2077600,
        2194400, 2325600, 2455000, 2645000, 2845000, 3141100, 3404710, 3782160, 4151400, 4604100, 5057300, 5533840,
        6087120, 6685120, 7352800, 8080800, 8725600, 9578400, 10545600, 11585600, 12740000, 14418250, 16280000,
        21196500, 23315500, 25649000, 249232940
    };

    private static CharacterInfo currentCharacter;
    private static boolean inCharacterSelection;

    /** Needed for all Models */
    public static void init() {}

    public static boolean hasCharacter() {
        return currentCharacter != null;
    }

    public static CharacterInfo getCharacterInfo() {
        return currentCharacter;
    }

    @SubscribeEvent
    public static void onMenuOpened(MenuOpenedEvent e) {
        if (e.getMenuType() == MenuType.GENERIC_9x3
                && ComponentUtils.getCoded(e.getTitle()).equals("§8§lSelect a Class")) {
            inCharacterSelection = true;
            WynntilsMod.info("In character selection menu");
        }
    }

    @SubscribeEvent
    public static void onMenuClosed(MenuClosedEvent e) {
        inCharacterSelection = false;
    }

    @SubscribeEvent
    public static void onWorldStateChanged(WorldStateEvent e) {
        // Whenever we're leaving a world, clear the current character
        if (e.getOldState() == WorldStateManager.State.WORLD) {
            currentCharacter = null;
            // This should not be needed, but have it as a safeguard
            inCharacterSelection = false;
        }
        if (e.getNewState() == WorldStateManager.State.CHARACTER_SELECTION) {
            WynntilsMod.info("Preparing for character selection");
        }
    }

    @SubscribeEvent
    public static void onContainerClick(ContainerClickEvent e) {
        if (inCharacterSelection) {
            if (e.getItemStack().getItem() == Items.AIR) return;
            currentCharacter = CharacterInfo.parseCharacter(e.getItemStack(), e.getSlotNum());
            WynntilsMod.info("Selected character " + currentCharacter);
        }
    }

    // TODO: We don't have a way to parse CharacterInfo if auto select class is on for the player
    //       Fix this by storing last selected class in WebAPI.
    public static final class CharacterInfo {
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

        /** Returns the current class name, wrt reskinned or not.
         */
        public String getActualName() {
            return classType.getActualName(reskinned);
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
            String className = "";

            for (String line : lore) {
                Matcher levelMatcher = LEVEL_PATTERN.matcher(line);
                if (levelMatcher.matches()) {
                    level = Integer.parseInt(levelMatcher.group(1));
                    continue;
                }

                Matcher classMatcher = CLASS_PATTERN.matcher(line);

                if (classMatcher.matches()) {
                    className = classMatcher.group(1);
                }
            }
            ClassType classType = ClassType.fromName(className);

            return new CharacterInfo(classType, classType != null && ClassType.isReskinned(className), level, slotNum);
        }

        @Override
        public String toString() {
            return "CharacterInfo[classType=" + classType + ", reskinned=" + reskinned + ", level=" + level + ", id="
                    + id + ']';
        }

        /**
         * Return the maximum number of soul points the character can currently have
         */
        public int getMaxSoulPoints() {
            // FIXME: If player is veteran, we should always return 15
            int maxIfNotVeteran = 10 + MathUtils.clamp(getLevel() / 15, 0, 5);
            if (getSoulPoints() > maxIfNotVeteran) {
                return 15;
            }
            return maxIfNotVeteran;
        }

        /**
         * Return the current number of soul points of the character, or -1 if unable to determine
         */
        public int getSoulPoints() {
            ItemStack soulPoints = McUtils.player().getInventory().getItem(8);
            if (soulPoints.getItem() != Items.NETHER_STAR) {
                return -1;
            }

            return soulPoints.getCount();
        }

        /**
         * Return the time in game ticks (1/20th of a second, 50ms) until the next soul point is given
         *
         * Also check that {@code {@link #getMaxSoulPoints()} >= {@link #getSoulPoints()}},
         * in which case soul points are already full
         */
        public int getTicksToNextSoulPoint() {
            if (McUtils.mc().level == null) return -1;
            return 24000 - (int) (McUtils.mc().level.getDayTime() % 24000);
        }

        public float getCurrentXp() {
            // We calculate our level in points by seeing how far we've progress towards our
            // current XP level's max
            return getXpProgress() * getXpPointsNeededToLevelUp();
        }

        public float getXpProgress() {
            return McUtils.player().experienceProgress;
        }

        public int getXpLevel() {
            return McUtils.player().experienceLevel;
        }

        public int getXpPointsNeededToLevelUp() {
            int levelIndex = getXpLevel() - 1;
            if (levelIndex >= LEVEL_UP_XP_REQUIREMENTS.length) {
                return Integer.MAX_VALUE;
            }
            return LEVEL_UP_XP_REQUIREMENTS[getXpLevel() - 1];
        }
    }
}
