/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.container.ScriptedContainerQuery;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import com.wynntils.models.character.actionbar.CoordinatesSegment;
import com.wynntils.models.character.actionbar.HealthSegment;
import com.wynntils.models.character.actionbar.ManaSegment;
import com.wynntils.models.character.actionbar.PowderSpecialSegment;
import com.wynntils.models.character.actionbar.SprintSegment;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.event.StatusEffectsChangedEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.character.type.StatusEffect;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.experience.CombatXpModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CharacterModel extends Model {
    private static final Pattern CLASS_MENU_CLASS_PATTERN = Pattern.compile("§e- §r§7Class: §r§f(.+)");
    private static final Pattern CLASS_MENU_LEVEL_PATTERN = Pattern.compile("§e- §r§7Level: §r§f(\\d+)");
    private static final Pattern INFO_MENU_CLASS_PATTERN = Pattern.compile("§7Class: §r§f(.+)");
    private static final Pattern INFO_MENU_LEVEL_PATTERN = Pattern.compile("§7Combat Lv: §r§f(\\d+)");

    private static final int CHARACTER_INFO_SLOT = 7;
    private static final int SOUL_POINT_SLOT = 8;
    private static final int PROFESSION_INFO_SLOT = 17;

    /**
     * CG1 is the color and symbol used for the effect, and the strength modifier string (e.g. "79%")
     * NCG1 is for strength modifiers without a decimal, and the % sign
     * NCG2 is the decimal point and second \d+ option for strength modifiers with a decimal
     * CG2 is the actual name of the effect
     * CG3 is the duration string (eg. "1:23")
     * Note: Buffs like "+190 Main Attack Damage" will have the +190 be considered as part of the name.
     * Buffs like "17% Frenzy" will have the 17% be considered as part of the prefix.
     * This is because the 17% in Frenzy (and certain other buffs) can change, but the static scroll buffs cannot.
     * <p>
     * https://regexr.com/7999h
     *
     * <p>Originally taken from: <a href="https://github.com/Wynntils/Wynntils/pull/615">Legacy</a>
     */
    private static final Pattern STATUS_EFFECT_PATTERN =
            Pattern.compile("(.+?§7 ??(?:\\d+(?:\\.\\d+)?%)?) ?([%\\-+\\/\\da-zA-Z'\\s]+?) §[84a]\\((.+?)\\).*");

    private static final String STATUS_EFFECTS_TITLE = "§d§lStatus Effects";

    private final CoordinatesSegment coordinatesSegment = new CoordinatesSegment(this::centerSegmentCleared);
    private final HealthSegment healthSegment = new HealthSegment();
    private final ManaSegment manaSegment = new ManaSegment();
    private final PowderSpecialSegment powderSpecialSegment = new PowderSpecialSegment();
    private final SprintSegment sprintSegment = new SprintSegment();

    private boolean inCharacterSelection;
    private boolean hasCharacter;

    private ClassType classType;
    private boolean reskinned;
    private int level;

    // This field is basically the slot id of the class,
    // meaning that if a class changes slots, the ID will not be persistent.
    // This was implemented the same way by legacy.
    private String id;

    private List<StatusEffect> statusEffects = new ArrayList<>();

    public CharacterModel(CombatXpModel combatXpModel) {
        super(List.of(combatXpModel));

        Handlers.ActionBar.registerSegment(coordinatesSegment);
        Handlers.ActionBar.registerSegment(healthSegment);
        Handlers.ActionBar.registerSegment(manaSegment);
        Handlers.ActionBar.registerSegment(powderSpecialSegment);
        Handlers.ActionBar.registerSegment(sprintSegment);
    }

    public List<StatusEffect> getStatusEffects() {
        return statusEffects;
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
        int maxIfNotVeteran = 10 + MathUtils.clamp(Models.CombatXp.getXpLevel() / 15, 0, 5);
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

    public String getId() {
        // We can't return an empty string, otherwise we risk making our config file messed up (empty string map key for
        // ItemLockFeature)
        if (!hasCharacter) return "-";

        return id;
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
            WynntilsMod.info("Scheduling character info query");

            // We need to scan character info and profession info as well.
            scanCharacterInfoPage();

            // We need to parse the current character id from our inventory
            updateCharacterId();
        }
    }

    @SubscribeEvent
    public void onTabListCustomization(PlayerInfoFooterChangedEvent event) {
        String footer = event.getFooter();

        if (footer.isEmpty()) {
            if (!statusEffects.isEmpty()) {
                statusEffects = new ArrayList<>(); // No timers, get rid of them
                WynntilsMod.postEvent(new StatusEffectsChangedEvent());
            }

            return;
        }

        if (!footer.startsWith(STATUS_EFFECTS_TITLE)) return;

        List<StatusEffect> newStatusEffects = new ArrayList<>();

        String[] effects = footer.split("\\s{2}"); // Effects are split up by 2 spaces
        for (String effect : effects) {
            String trimmedEffect = effect.trim();
            if (trimmedEffect.isEmpty()) continue;

            Matcher m = STATUS_EFFECT_PATTERN.matcher(trimmedEffect);
            if (!m.find()) continue;

            // See comment at TAB_EFFECT_PATTERN definition for format description of these
            String prefix = m.group(1);
            String name = m.group(2);
            String displayedTime = m.group(3);
            newStatusEffects.add(new StatusEffect(name, displayedTime, prefix));
        }

        statusEffects = newStatusEffects;
        WynntilsMod.postEvent(new StatusEffectsChangedEvent());
    }

    private void scanCharacterInfoPage() {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Character Info Query")
                .useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                .matchTitle("Character Info")
                .processContainer(container -> {
                    ItemStack characterInfoItem = container.items().get(CHARACTER_INFO_SLOT);
                    ItemStack professionInfoItem = container.items().get(PROFESSION_INFO_SLOT);

                    Models.Profession.resetValueFromItem(professionInfoItem);

                    parseCharacterFromCharacterMenu(characterInfoItem);
                    hasCharacter = true;
                    WynntilsMod.postEvent(new CharacterUpdateEvent());
                    WynntilsMod.info("Deducing character " + getCharacterString());
                })
                .onError(msg -> WynntilsMod.warn("Error querying Character Info:" + msg))
                .build();
        query.executeQuery();
    }

    private void updateCharacterId() {
        ItemStack soulPointItem = McUtils.inventory().items.get(SOUL_POINT_SLOT);

        LinkedList<String> soulLore = LoreUtils.getLore(soulPointItem);

        String id = "";
        for (String line : soulLore) {
            if (line.startsWith(ChatFormatting.DARK_GRAY.toString())) {
                id = ComponentUtils.stripFormatting(line);
                break;
            }
        }

        WynntilsMod.info("Selected character: " + id);

        this.id = id;
    }

    private String getCharacterString() {
        return "CharacterInfo{" + "classType="
                + classType + ", reskinned="
                + reskinned + ", level="
                + level + ", id="
                + id + '}';
    }

    private void parseCharacterFromCharacterMenu(ItemStack characterInfoItem) {
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

        updateCharacterInfo(classType, classType != null && ClassType.isReskinned(className), level);
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

        updateCharacterInfo(classType, classType != null && ClassType.isReskinned(className), level);
    }

    private void updateCharacterInfo(ClassType classType, boolean reskinned, int level) {
        this.classType = classType;
        this.reskinned = reskinned;
        this.level = level;
    }
}
