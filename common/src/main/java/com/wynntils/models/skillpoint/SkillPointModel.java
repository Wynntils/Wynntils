package com.wynntils.models.skillpoint;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.items.gui.SkillPointItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SkillPointModel extends Model {
    private static final int TOME_SLOT = 8;
    private static final int[] SKILL_POINT_TIME_SLOTS = {4, 11, 19};

    private final Map<Skill, Integer> totalSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> gearSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> tomeSkillPoints = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> assignedSkillPoints = new EnumMap<>(Skill.class);

    public SkillPointModel() {
        super(List.of());
    }

    public void updateTotals(ItemStack[] skillPointItems) {
        for (int i = 0; i < 5; i++) {
            Optional<WynnItem> wynnItemOptional = Models.Item.getWynnItem(skillPointItems[i]);
            if (wynnItemOptional.isPresent() && wynnItemOptional.get() instanceof SkillPointItem skillPoint) {
                totalSkillPoints.put(skillPoint.getSkill(), skillPoint.getSkillPoints());
            } else {
                WynntilsMod.warn("Failed to parse skill point item: " + LoreUtils.getStringLore(skillPointItems[i]));
            }
        }
    }

    public void calculateGearSkillPoints() {
        gearSkillPoints.clear();
        McUtils.inventory().armor.forEach(itemStack -> {
            Optional<WynnItem> wynnItemOptional = Models.Item.getWynnItem(itemStack);
            if (wynnItemOptional.isEmpty()) return; // Empty slot
            if (wynnItemOptional.get() instanceof GearItem gearItem) {
                for (Skill skill : Skill.values()) {
                    gearSkillPoints.merge(skill, gearItem.getGearInfo().fixedStats().getSkillBonus(skill), Integer::sum);
                }
            } else {
                WynntilsMod.warn("Failed to parse armour: " + LoreUtils.getStringLore(itemStack));
            }
        });

        // 27-30 are accessories
        for (int i = 27; i <= 30; i++) { // fixme this doesn't work??
            Optional<WynnItem> wynnItemOptional = Models.Item.getWynnItem(McUtils.inventory().getItem(i));
            if (wynnItemOptional.isEmpty()) continue; // Empty slot
            if (wynnItemOptional.get() instanceof GearItem gearItem) {
                for (Skill skill : Skill.values()) {
                    gearSkillPoints.merge(skill, gearItem.getGearInfo().fixedStats().getSkillBonus(skill), Integer::sum);
                }
            } else {
                WynntilsMod.warn("Failed to parse accessory: " + LoreUtils.getStringLore(McUtils.inventory().getItem(i)));
            }
        }
    }

    public void queryTomeSkillPoints() { // fixme this also doesn't work
        // doesn't like having another container open
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Tome Skill Point Query")
                .onError(msg -> WynntilsMod.warn("Failed to query tome skill points: " + msg))

                .then(QueryStep.clickOnSlot(TOME_SLOT)
                        .expectContainerTitle("Mastery Tomes")
                        .processIncomingContainer(this::processTomeSkillPoints))
                .build();

        query.executeQuery();
    }

    private void processTomeSkillPoints(ContainerContent content) {
        tomeSkillPoints.clear();
        for (Integer slot : SKILL_POINT_TIME_SLOTS) {
            Optional<WynnItem> wynnItemOptional = Models.Item.getWynnItem(content.items().get(slot));
            if (wynnItemOptional.isPresent() && wynnItemOptional.get() instanceof TomeItem tome) {
                for (Skill skill : Skill.values()) {
                    tomeSkillPoints.merge(skill, 2, Integer::sum);
                }
            } else {
                WynntilsMod.warn("Failed to parse tome: " + LoreUtils.getStringLore(content.items().get(slot)));
            }
        }
    }

    public int getTotalSkillPoints(Skill skill) {
        return totalSkillPoints.getOrDefault(skill, 0);
    }

    public int getGearSkillPoints(Skill skill) {
        return gearSkillPoints.getOrDefault(skill, 0);
    }

    public int getTomeSkillPoints(Skill skill) {
        return tomeSkillPoints.getOrDefault(skill, 0);
    }

    public int getAssignedSkillPoints(Skill skill) {
        return assignedSkillPoints.getOrDefault(skill, 0);
    }
}
