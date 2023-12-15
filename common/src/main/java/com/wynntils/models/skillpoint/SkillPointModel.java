/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.skillpoint;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

public class SkillPointModel extends Model {
    private static final int TOME_SLOT = 8;
    private static final int[] SKILL_POINT_TOTAL_SLOTS = {11, 12, 13, 14, 15};
    private static final int[] SKILL_POINT_TOME_SLOTS = {4, 11, 19};

    private final Map<Skill, Integer> totalSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> gearSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> craftedSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> tomeSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> assignedSkillPoints = new EnumMap<>(Skill.class);

    public SkillPointModel() {
        super(List.of());
    }

    public void calculateAssignedSkillPoints() {
        // No .closeContainer() here, we want the screen to remain open but close the inventory in the background
        McUtils.player()
                .connection
                .send(new ServerboundContainerClosePacket(McUtils.player().containerMenu.containerId));
        McUtils.player().containerMenu = McUtils.player().inventoryMenu;

        Managers.TickScheduler.scheduleNextTick(() -> {
            querySkillPoints();
            calculateGearSkillPoints();
            for (Skill skill : Skill.values()) {
                assignedSkillPoints.put(
                        skill, getTotalSkillPoints(skill) - getGearSkillPoints(skill) - getTomeSkillPoints(skill));
            }
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
            } else {
                WynntilsMod.warn("Failed to parse armour: " + LoreUtils.getStringLore(itemStack));
            }
        });

        for (int i = 9; i <= 12; i++) {
            Optional<WynnItem> wynnItemOptional =
                    Models.Item.getWynnItem(McUtils.inventory().getItem(i));
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
            } else {
                WynntilsMod.warn("Failed to parse accessory: "
                        + LoreUtils.getStringLore(McUtils.inventory().getItem(i)));
            }
        }
    }

    private void querySkillPoints() {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Skill Point Query")
                .onError(msg -> WynntilsMod.warn("Failed to query skill points: " + msg))
                .then(QueryStep.useItemInHotbar(CharacterModel.CHARACTER_INFO_SLOT - 1)
                        .expectContainerTitle("Character Info")
                        .processIncomingContainer(this::processTotalSkillPoints))
                .then(QueryStep.clickOnSlot(TOME_SLOT)
                        .expectContainerTitle("Mastery Tomes")
                        .processIncomingContainer(this::processTomeSkillPoints))
                .build();

        query.executeQuery();
    }

    private void processTotalSkillPoints(ContainerContent content) {
        System.out.println("querying total skill points");
        totalSkillPoints.clear();
        for (Integer slot : SKILL_POINT_TOTAL_SLOTS) {
            Optional<WynnItem> wynnItemOptional =
                    Models.Item.getWynnItem(content.items().get(slot));
            if (wynnItemOptional.isPresent() && wynnItemOptional.get() instanceof SkillPointItem skillPoint) {
                totalSkillPoints.merge(skillPoint.getSkill(), skillPoint.getSkillPoints(), Integer::sum);
            } else {
                WynntilsMod.warn("Failed to parse skill point item: "
                        + LoreUtils.getStringLore(content.items().get(slot)));
            }
        }

        System.out.println("total skill points: " + totalSkillPoints);
    }

    private void processTomeSkillPoints(ContainerContent content) {
        System.out.println("querying tomes");
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
                WynntilsMod.warn("Failed to parse tome: "
                        + LoreUtils.getStringLore(content.items().get(slot)));
            }
        }
        System.out.println("tome skill points: " + tomeSkillPoints);
    }

    public int getTotalSkillPoints(Skill skill) {
        return totalSkillPoints.getOrDefault(skill, 0);
    }

    public int getGearSkillPoints(Skill skill) {
        return gearSkillPoints.getOrDefault(skill, 0);
    }

    public int getCraftedSkillPoints(Skill skill) {
        return craftedSkillPoints.getOrDefault(skill, 0);
    }

    public int getTomeSkillPoints(Skill skill) {
        return tomeSkillPoints.getOrDefault(skill, 0);
    }

    public int getAssignedSkillPoints(Skill skill) {
        return assignedSkillPoints.getOrDefault(skill, 0);
    }
}
