/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.container.ScriptedContainerQuery;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.models.character.actionbar.CoordinatesSegment;
import com.wynntils.models.character.actionbar.HealthSegment;
import com.wynntils.models.character.actionbar.ManaSegment;
import com.wynntils.models.character.actionbar.PowderSpecialSegment;
import com.wynntils.models.character.actionbar.SprintSegment;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.concepts.Powder;
import com.wynntils.models.concepts.ProfessionInfo;
import com.wynntils.models.concepts.ProfessionType;
import com.wynntils.models.spells.actionbar.SpellSegment;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CharacterModel extends Model {
    private static final Pattern CLASS_MENU_CLASS_PATTERN = Pattern.compile("§e- §r§7Class: §r§f(.+)");
    private static final Pattern CLASS_MENU_LEVEL_PATTERN = Pattern.compile("§e- §r§7Level: §r§f(\\d+)");
    private static final Pattern INFO_MENU_CLASS_PATTERN = Pattern.compile("§7Class: §r§f(.+)");
    private static final Pattern INFO_MENU_LEVEL_PATTERN = Pattern.compile("§7Combat Lv: §r§f(\\d+)");
    private static final Pattern INFO_MENU_PROFESSION_LORE_PATTERN =
            Pattern.compile("§6- §r§7[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] Lv. (\\d+) (.+)§r§8 \\[([\\d.]+)%\\]");
    private static final int CHARACTER_INFO_SLOT = 7;
    private static final int PROFESSION_INFO_SLOT = 17;

    /* These values are copied from a post by Salted, https://forums.wynncraft.com/threads/new-levels-xp-requirement.261763/
     * which points to the data at https://pastebin.com/fCTfEkaC
     * Note that the last value is the sum of all preceding values
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

    private final CoordinatesSegment coordinatesSegment = new CoordinatesSegment(this::centerSegmentCleared);
    private final HealthSegment healthSegment = new HealthSegment();
    private final ManaSegment manaSegment = new ManaSegment();
    private final PowderSpecialSegment powderSpecialSegment = new PowderSpecialSegment();
    private final SpellSegment spellSegment = new SpellSegment();
    private final SprintSegment sprintSegment = new SprintSegment();

    private boolean inCharacterSelection;
    private boolean hasCharacter;

    private ClassType classType;
    private boolean reskinned;
    private int level;

    // This field is basically the slot id of the class,
    // meaning that if a class changes slots, the ID will not be persistent.
    // This was implemented the same way by legacy.
    private int id;

    private ProfessionInfo professionInfo;

    public CharacterModel() {
        Handlers.ActionBar.registerSegment(coordinatesSegment);
        Handlers.ActionBar.registerSegment(healthSegment);
        Handlers.ActionBar.registerSegment(manaSegment);
        Handlers.ActionBar.registerSegment(powderSpecialSegment);
        Handlers.ActionBar.registerSegment(spellSegment);
        Handlers.ActionBar.registerSegment(sprintSegment);
    }

    public int getCurrentHealth() {
        return healthSegment.getCurrentHealth();
    }

    public int getMaxHealth() {
        return healthSegment.getMaxHealth();
    }

    public int getCurrentMana() {
        return manaSegment.getCurrentMana();
    }

    public int getMaxMana() {
        return manaSegment.getMaxMana();
    }

    public float getPowderSpecialCharge() {
        return powderSpecialSegment.getPowderSpecialCharge();
    }

    public Powder getPowderSpecialType() {
        return powderSpecialSegment.getPowderSpecialType();
    }

    public void hideHealth(boolean shouldHide) {
        healthSegment.setHidden(shouldHide);
    }

    public void hideMana(boolean shouldHide) {
        manaSegment.setHidden(shouldHide);
    }

    /**
     * Return the maximum number of soul points the character can currently have
     */
    public int getMaxSoulPoints() {
        // FIXME: If player is veteran, we should always return 15
        int maxIfNotVeteran = 10 + MathUtils.clamp(getXpLevel() / 15, 0, 5);
        if (getSoulPoints() > maxIfNotVeteran) {
            return 15;
        }
        return maxIfNotVeteran;
    }

    /**
     * Return the current number of soul points of the character, or -1 if unable to determine
     */
    public int getSoulPoints() {
        ItemStack soulPoints = McUtils.inventory().getItem(8);
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
        if (levelIndex < 0) {
            return 0;
        }
        return LEVEL_UP_XP_REQUIREMENTS[levelIndex];
    }

    public ClassType getClassType() {
        if (!hasCharacter) return ClassType.None;

        return classType;
    }

    public boolean isReskinned() {
        if (!hasCharacter) return false;

        return reskinned;
    }

    /** Returns the current class name, wrt reskinned or not.
     */
    public String getActualName() {
        return getClassType().getActualName(isReskinned());
    }

    public int getId() {
        if (!hasCharacter) return 0;

        return id;
    }

    public ProfessionInfo getProfessionInfo() {
        if (!hasCharacter) return new ProfessionInfo();

        return professionInfo;
    }

    @SubscribeEvent
    public void onMenuClosed(MenuClosedEvent e) {
        inCharacterSelection = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldStateChanged(WorldStateEvent e) {
        // Whenever we're leaving a world, clear the current character
        if (e.getOldState() == WorldState.WORLD) {
            hasCharacter = false;
            // This should not be needed, but have it as a safeguard
            inCharacterSelection = false;
        }

        if (e.getNewState() == WorldState.CHARACTER_SELECTION) {
            inCharacterSelection = true;
        }

        if (e.getNewState() == WorldState.WORLD) {
            if (e.getOldState() != WorldState.CHARACTER_SELECTION) {
                // We went directly to a world without coming from the character selection
                // menu. This means the player has "autojoin" enabled, and that we did not
                // get a chance to read the character info from the character selection menu.
                // Instead, we send a container query to read it from the character (compass) menu.
                WynntilsMod.info("Scheduling character info query");

                // This time, we need to scan character info and profession info as well.
                scanCharacterInfoPage(-1);
            } else {
                // We did not auto-join, we have a correct ID already.
                int oldId = getId();
                scanCharacterInfoPage(oldId);
            }
        }
    }

    private void scanCharacterInfoPage(int oldId) {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Character Info Query")
                .useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                .matchTitle("Character Info")
                .processContainer(container -> {
                    ItemStack characterInfoItem = container.items().get(CHARACTER_INFO_SLOT);
                    ItemStack professionInfoItem = container.items().get(PROFESSION_INFO_SLOT);

                    // FIXME: When we can calculate id here, check if calculated id is -1, if not use it, otherwise
                    // default to oldId
                    parseCharacterFromCharacterMenu(characterInfoItem, professionInfoItem, oldId);
                    hasCharacter = true;
                    WynntilsMod.postEvent(new CharacterUpdateEvent());
                    WynntilsMod.info("Deducing character " + getCharacterString());
                })
                .onError(msg -> WynntilsMod.warn("Error querying Character Info:" + msg))
                .build();
        query.executeQuery();
    }

    private String getCharacterString() {
        return "CharacterInfo{" + "classType="
                + classType + ", reskinned="
                + reskinned + ", level="
                + level + ", id="
                + id + ", professionInfo="
                + professionInfo + '}';
    }

    private void parseCharacterFromCharacterMenu(ItemStack characterInfoItem, ItemStack professionInfoItem, int id) {
        List<String> lore = LoreUtils.getLore(characterInfoItem);

        int level = 0;
        String className = "";

        for (String line : lore) {
            Matcher levelMatcher = INFO_MENU_LEVEL_PATTERN.matcher(line);
            if (levelMatcher.matches()) {
                level = Integer.parseInt(levelMatcher.group(1));
                continue;
            }

            Matcher classMatcher = INFO_MENU_CLASS_PATTERN.matcher(line);

            if (classMatcher.matches()) {
                className = classMatcher.group(1);
            }
        }
        ClassType classType = ClassType.fromName(className);

        Map<ProfessionType, Integer> levels = new HashMap<>();
        List<String> professionLore = LoreUtils.getLore(professionInfoItem);
        for (String line : professionLore) {
            Matcher matcher = INFO_MENU_PROFESSION_LORE_PATTERN.matcher(line);

            if (matcher.matches()) {
                levels.put(ProfessionType.fromString(matcher.group(2)), Integer.parseInt(matcher.group(1)));
            }
        }

        updateCharacterInfo(
                classType,
                classType != null && ClassType.isReskinned(className),
                level,
                id,
                new ProfessionInfo(levels));
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent e) {
        if (inCharacterSelection) {
            if (e.getItemStack().getItem() == Items.AIR) return;
            parseCharacter(e.getItemStack(), e.getSlotNum());
            hasCharacter = true;
            WynntilsMod.postEvent(new CharacterUpdateEvent());
            WynntilsMod.info("Selected character " + getCharacterString());
        }
    }

    private void centerSegmentCleared() {
        powderSpecialSegment.replaced();
    }

    private void parseCharacter(ItemStack itemStack, int id) {
        List<String> lore = LoreUtils.getLore(itemStack);

        int level = 0;
        String className = "";

        for (String line : lore) {
            Matcher levelMatcher = CLASS_MENU_LEVEL_PATTERN.matcher(line);
            if (levelMatcher.matches()) {
                level = Integer.parseInt(levelMatcher.group(1));
                continue;
            }

            Matcher classMatcher = CLASS_MENU_CLASS_PATTERN.matcher(line);

            if (classMatcher.matches()) {
                className = classMatcher.group(1);
            }
        }
        ClassType classType = ClassType.fromName(className);

        updateCharacterInfo(
                classType, classType != null && ClassType.isReskinned(className), level, id, new ProfessionInfo());
    }

    private void updateCharacterInfo(
            ClassType classType, boolean reskinned, int level, int id, ProfessionInfo professionInfo) {
        this.classType = classType;
        this.reskinned = reskinned;
        this.level = level;
        this.professionInfo = professionInfo;
        this.id = id;
    }
}
