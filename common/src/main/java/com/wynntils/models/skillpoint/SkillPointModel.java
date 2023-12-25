/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.skillpoint;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.items.gui.SkillPointItem;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class SkillPointModel extends Model {
    @Persisted
    private final Storage<Map<String, SavableSkillPointSet>> skillPointLoadouts = new Storage<>(new LinkedHashMap<>());

    private static final int[] ACCESSORY_SLOTS = {9, 10, 11, 12};
    private static final int TOME_SLOT = 8;
    private static final int[] SKILL_POINT_TOTAL_SLOTS = {11, 12, 13, 14, 15};
    private static final int[] SKILL_POINT_TOME_SLOTS = {4, 11, 19};

    private final Map<Skill, Integer> totalSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> gearSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> craftedSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> tomeSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> assignedSkillPoints = new EnumMap<>(Skill.class);
    private boolean tomesUnlocked = true;

    public SkillPointModel() {
        super(List.of());
    }

    public boolean hasLoadout(String name) {
        return skillPointLoadouts.get().containsKey(name);
    }

    /**
     * Saves the current assigned skill points to the loadout list.
     * @param name The name of the loadout to save.
     */
    public void saveCurrentSkillPoints(String name) {
        SavableSkillPointSet assignedSkillPointSet = new SavableSkillPointSet(
                getAssignedSkillPoints(Skill.STRENGTH),
                getAssignedSkillPoints(Skill.DEXTERITY),
                getAssignedSkillPoints(Skill.INTELLIGENCE),
                getAssignedSkillPoints(Skill.DEFENCE),
                getAssignedSkillPoints(Skill.AGILITY));
        skillPointLoadouts.get().put(name, assignedSkillPointSet);
        WynntilsMod.info("Saved skill point loadout: " + name + " " + assignedSkillPointSet);
    }

    /**
     * Saves the current assigned skill points and gear to the build list.
     * @param name The name of the build to save.
     */
    public void saveCurrentBuild(String name) {
        List<String> armourNames = new ArrayList<>();
        McUtils.inventory().armor.stream()
                .map(x -> x.getHoverName().getString())
                .forEach(armourNames::add);
        Collections.reverse(armourNames); // helmet to boots order

        List<String> accessoryNames = new ArrayList<>();
        for (int i : ACCESSORY_SLOTS) {
            ItemStack itemStack = McUtils.inventory().getItem(i);
            if (!itemStack.isEmpty()) {
                accessoryNames.add(itemStack.getHoverName().getString());
            }
        }

        SavableSkillPointSet assignedSkillPointSet = new SavableSkillPointSet(
                getAssignedSkillPoints(Skill.STRENGTH),
                getAssignedSkillPoints(Skill.DEXTERITY),
                getAssignedSkillPoints(Skill.INTELLIGENCE),
                getAssignedSkillPoints(Skill.DEFENCE),
                getAssignedSkillPoints(Skill.AGILITY),
                armourNames,
                accessoryNames);

        skillPointLoadouts.get().put(name, assignedSkillPointSet);
        WynntilsMod.info("Saved skill point build: " + name + " - " + assignedSkillPointSet);
    }

    public void loadLoadout(String name) {
        McUtils.closeBackgroundContainer();

        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Loading Skill Point Loadout Query")
                .onError(msg -> WynntilsMod.warn("Failed to load skill point loadout: " + msg))
                .then(QueryStep.useItemInHotbar(CharacterModel.CHARACTER_INFO_SLOT - 1)
                        .expectContainerTitle("Character Info")
                        .processIncomingContainer((container) -> loadSkillPointsOnServer(container, name)))
                .build();
        query.executeQuery();
    }

    private void loadSkillPointsOnServer(ContainerContent containerContent, String name) {
        // we need to figure out which points we can subtract from first to actually allow assigning for positive points
        Map<Skill, Integer> negatives = new EnumMap<>(Skill.class);
        Map<Skill, Integer> positives = new EnumMap<>(Skill.class);
        for (int i = 0; i < 5; i++) {
            int buildTarget = skillPointLoadouts.get().get(name).getSkillPointsAsArray()[i];
            int difference = buildTarget - getAssignedSkillPoints(Skill.values()[i]);

            // no difference automatically dropped here
            if (difference > 0) {
                positives.put(Skill.values()[i], difference);
            } else if (difference < 0) {
                negatives.put(Skill.values()[i], difference);
            }
        }

        AtomicBoolean confirmationCompleted = new AtomicBoolean(false);
        negatives.forEach((skill, difference) -> {
            int difference5s = Math.abs(difference) / 5;
            int difference1s = Math.abs(difference) % 5;

            for (int i = 0; i < difference5s; i++) {
                ContainerUtils.clickOnSlot(
                        SKILL_POINT_TOTAL_SLOTS[skill.ordinal()],
                        containerContent.containerId(),
                        GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                        containerContent.items(),
                        true);
                if (!confirmationCompleted.getAndSet(true)) {
                    // confirmation required, force loop to repeat this iteration
                    i--;
                }
            }
            for (int i = 0; i < difference1s; i++) {
                ContainerUtils.clickOnSlot(
                        SKILL_POINT_TOTAL_SLOTS[skill.ordinal()],
                        containerContent.containerId(),
                        GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                        containerContent.items(),
                        false);
                if (!confirmationCompleted.getAndSet(true)) {
                    // needs to exist in both loops in case of 1s only
                    i--;
                }
            }
        });

        positives.forEach((skill, difference) -> {
            int difference5s = difference / 5;
            int difference1s = difference % 5;

            for (int i = 0; i < difference5s; i++) {
                ContainerUtils.clickOnSlot(
                        SKILL_POINT_TOTAL_SLOTS[skill.ordinal()],
                        containerContent.containerId(),
                        GLFW.GLFW_MOUSE_BUTTON_LEFT,
                        containerContent.items(),
                        true);
            }
            for (int i = 0; i < difference1s; i++) {
                ContainerUtils.clickOnSlot(
                        SKILL_POINT_TOTAL_SLOTS[skill.ordinal()],
                        containerContent.containerId(),
                        GLFW.GLFW_MOUSE_BUTTON_LEFT,
                        containerContent.items(),
                        false);
            }
        });

        // Server needs 2 ticks, give one extra to be safe
        Managers.TickScheduler.scheduleLater(this::populateSkillPoints, 3);
    }

    public void deleteLoadout(String name) {
        skillPointLoadouts.get().remove(name);
    }

    /**
     * Closes any open containers (but not the screen shown) then queries compass (and tome menus) depending on if
     * tomes have been unlocked.
     */
    public void populateSkillPoints() {
        McUtils.closeBackgroundContainer();

        Managers.TickScheduler.scheduleNextTick(() -> {
            calculateGearSkillPoints();
            queryTotalAndTomeSkillPoints();
        });
    }

    private void calculateGearSkillPoints() {
        gearSkillPoints.clear();
        craftedSkillPoints.clear();

        // Cannot combine these loops because of the way the inventory is numbered when a container is open
        McUtils.inventory().armor.forEach(itemStack -> {
            Optional<WynnItem> wynnItemOptional = Models.Item.getWynnItem(itemStack);
            if (wynnItemOptional.isEmpty()) return; // Empty slot

            if (wynnItemOptional.get() instanceof GearItem gear) {
                gear.getIdentifications().forEach(x -> {
                    if (x.statType() instanceof SkillStatType skillStat) {
                        gearSkillPoints.merge(skillStat.getSkill(), x.value(), Integer::sum);
                    }
                });
            } else if (wynnItemOptional.get() instanceof CraftedGearItem craftedGear) {
                craftedGear.getIdentifications().forEach(x -> {
                    if (x.statType() instanceof SkillStatType skillStat) {
                        craftedSkillPoints.merge(skillStat.getSkill(), x.value(), Integer::sum);
                    }
                });
            } else if (!itemStack.isEmpty()) {
                WynntilsMod.warn("Skill Point Model failed to parse armour: " + LoreUtils.getStringLore(itemStack));
            }
        });

        for (int i : ACCESSORY_SLOTS) {
            ItemStack itemStack = McUtils.inventory().getItem(i);
            Optional<WynnItem> wynnItemOptional = Models.Item.getWynnItem(itemStack);
            if (wynnItemOptional.isEmpty()) continue; // Empty slot

            if (wynnItemOptional.get() instanceof GearItem gear) {
                gear.getIdentifications().forEach(x -> {
                    if (x.statType() instanceof SkillStatType skillStat) {
                        gearSkillPoints.merge(skillStat.getSkill(), x.value(), Integer::sum);
                    }
                });
            } else if (wynnItemOptional.get() instanceof CraftedGearItem craftedGear) {
                craftedGear.getIdentifications().forEach(x -> {
                    if (x.statType() instanceof SkillStatType skillStat) {
                        craftedSkillPoints.merge(skillStat.getSkill(), x.value(), Integer::sum);
                    }
                });
            } else if (!itemStack.isEmpty()
                    && !itemStack.getHoverName().getString().equals("§7Accessory Slot")) {
                WynntilsMod.warn("Skill Point Model failed to parse accessory: "
                        + LoreUtils.getStringLore(McUtils.inventory().getItem(i)));
            }
        }
    }

    /**
     * Queries the compass (and tome) menu for skill point data.
     */
    private void queryTotalAndTomeSkillPoints() {
        totalSkillPoints.clear();
        tomeSkillPoints.clear();

        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Total and Tome Skill Point Query")
                .onError(msg -> WynntilsMod.warn("Failed to query skill points: " + msg))
                .then(QueryStep.useItemInHotbar(CharacterModel.CHARACTER_INFO_SLOT - 1)
                        .expectContainerTitle("Character Info")
                        .processIncomingContainer(this::processTotalSkillPoints)
                        .saveContainer())
                .thenIf(this::areTomesUnlocked,
                        QueryStep.clickOnSlot(TOME_SLOT)
                        .expectContainerTitle("Mastery Tomes")
                        .processIncomingContainer(this::processTomeSkillPoints))
                .build();

        query.executeQuery();
    }

    private boolean areTomesUnlocked(ContainerContent content) {
        return LoreUtils.getStringLore(content.items().get(TOME_SLOT)).contains("✔");
    }

    private void processTotalSkillPoints(ContainerContent content) {
        for (Integer slot : SKILL_POINT_TOTAL_SLOTS) {
            Optional<WynnItem> wynnItemOptional =
                    Models.Item.getWynnItem(content.items().get(slot));
            if (wynnItemOptional.isPresent() && wynnItemOptional.get() instanceof SkillPointItem skillPoint) {
                totalSkillPoints.merge(skillPoint.getSkill(), skillPoint.getSkillPoints(), Integer::sum);
            } else {
                WynntilsMod.warn("Skill Point Model failed to parse skill point item: "
                        + LoreUtils.getStringLore(content.items().get(slot)));
            }
        }

        // Required for when tomes are not unlocked.
        // Does not matter when tomes are unlocked; that calculation is run later
        calculateAssignedSkillPoints();
    }

    private void processTomeSkillPoints(ContainerContent content) {
        tomeSkillPoints.clear();
        for (Integer slot : SKILL_POINT_TOME_SLOTS) {
            Optional<WynnItem> wynnItemOptional =
                    Models.Item.getWynnItem(content.items().get(slot));
            if (wynnItemOptional.isPresent() && wynnItemOptional.get() instanceof TomeItem tome) {
                tome.getIdentifications().forEach(x -> {
                    if (x.statType() instanceof SkillStatType skillStat) {
                        tomeSkillPoints.merge(skillStat.getSkill(), x.value(), Integer::sum);
                    }
                });
            } else {
                WynntilsMod.warn("Skill Point Model failed to parse tome: "
                        + LoreUtils.getStringLore(content.items().get(slot)));
            }
        }

        calculateAssignedSkillPoints();
    }

    private void calculateAssignedSkillPoints() {
        for (Skill skill : Skill.values()) {
            assignedSkillPoints.put(
                    skill,
                    getTotalSkillPoints(skill)
                            - getGearSkillPoints(skill)
                            - getTomeSkillPoints(skill)
                            - getCraftedSkillPoints(skill));
        }
    }

    public int getTotalSkillPoints(Skill skill) {
        return totalSkillPoints.getOrDefault(skill, 0);
    }

    public int getTotalSum() {
        return totalSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public int getGearSkillPoints(Skill skill) {
        return gearSkillPoints.getOrDefault(skill, 0);
    }

    public int getGearSum() {
        return gearSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public int getCraftedSkillPoints(Skill skill) {
        return craftedSkillPoints.getOrDefault(skill, 0);
    }

    public int getCraftedSum() {
        return craftedSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public int getTomeSkillPoints(Skill skill) {
        return tomeSkillPoints.getOrDefault(skill, 0);
    }

    public int getTomeSum() {
        return tomeSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public int getAssignedSkillPoints(Skill skill) {
        return assignedSkillPoints.getOrDefault(skill, 0);
    }

    public int getAssignedSum() {
        return assignedSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public Map<String, SavableSkillPointSet> getLoadouts() {
        return skillPointLoadouts.get();
    }
}
