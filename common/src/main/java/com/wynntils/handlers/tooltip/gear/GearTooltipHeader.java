/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.gear;

import com.wynntils.core.components.Models;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class GearTooltipHeader {
    public static List<Component> buildTooltip(GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        List<Component> header = new ArrayList<>();

        // name
        String prefix = gearInstance == null && !hideUnidentified ? "Unidentified " : "";
        header.add(Component.literal(prefix + gearInfo.name())
                .withStyle(gearInfo.tier().getChatFormatting()));

        // attack speed
        if (gearInfo.fixedStats().attackSpeed().isPresent())
            header.add(Component.literal(ChatFormatting.GRAY
                    + gearInfo.fixedStats().attackSpeed().get().getName()));

        header.add(Component.literal(""));

        // elemental damages
        if (!gearInfo.fixedStats().damages().isEmpty()) {
            List<Pair<DamageType, RangedValue>> damages = gearInfo.fixedStats().damages();
            for (Pair<DamageType, RangedValue> damageStat : damages) {
                DamageType type = damageStat.key();
                MutableComponent damage = Component.literal(type.getSymbol() + " " + type.getDisplayName())
                        .withStyle(type.getColorCode());
                damage.append(Component.literal("Damage: " + damageStat.value().asString())
                        .withStyle(
                                type == DamageType.NEUTRAL
                                        ? type.getColorCode()
                                        : ChatFormatting.GRAY)); // neutral is all gold
                header.add(damage);
            }

            header.add(Component.literal(""));
        }

        // health
        int health = gearInfo.fixedStats().healthBuff();
        if (health != 0) {
            MutableComponent healthComp = Component.literal("❤ Health: " + StringUtils.toSignedString(health))
                    .withStyle(ChatFormatting.DARK_RED);
            header.add(healthComp);
        }

        // elemental defenses
        if (!gearInfo.fixedStats().defences().isEmpty()) {
            List<Pair<Element, Integer>> defenses = gearInfo.fixedStats().defences();
            for (Pair<Element, Integer> defenceStat : defenses) {
                Element element = defenceStat.key();
                MutableComponent defense = Component.literal(element.getSymbol() + " " + element.getDisplayName())
                        .withStyle(element.getColorCode());
                defense.append(Component.literal(" Defence: " + StringUtils.toSignedString(defenceStat.value()))
                        .withStyle(ChatFormatting.GRAY));
                header.add(defense);
            }

            header.add(Component.literal(""));
        }

        // requirements
        int requirementsCount = 0;
        GearRequirements requirements = gearInfo.requirements();
        if (requirements.classType().isPresent()) {
            ClassType classType = requirements.classType().get();
            boolean fulfilled = Models.Character.getClassType() == classType;
            header.add(buildRequirementLine("Class Req: " + classType.getFullName(), fulfilled));
            requirementsCount++;
        }
        if (requirements.quest().isPresent()) {
            String questName = requirements.quest().get();
            Optional<QuestInfo> quest = Models.Quest.getQuestFromName(questName);
            boolean fulfilled = quest.isPresent() && quest.get().getStatus() == ActivityStatus.COMPLETED;
            header.add(buildRequirementLine("Quest Req: " + questName, fulfilled));
            requirementsCount++;
        }
        int level = requirements.level();
        if (level != 0) {
            boolean fulfilled = Models.CombatXp.getCombatLevel().current() >= level;
            header.add(buildRequirementLine("Combat Lv. Min: " + level, fulfilled));
            requirementsCount++;
        }
        if (!requirements.skills().isEmpty()) {
            for (Pair<Skill, Integer> skillRequirement : requirements.skills()) {
                // FIXME: CharacterModel is still missing info about our skill points
                header.add(buildRequirementLine(
                        skillRequirement.key().getDisplayName() + " Min: " + skillRequirement.value(), false));
                requirementsCount++;
            }
        }
        if (requirementsCount > 0) {
            header.add(Component.literal(""));
        }

        // skill bonuses
        List<Pair<Skill, Integer>> skillBonuses = gearInfo.fixedStats().skillBonuses();
        if (!skillBonuses.isEmpty()) {
            for (Skill skill : Models.Element.getGearSkillOrder()) {
                int skillBonusValue = gearInfo.fixedStats().getSkillBonus(skill);
                if (skillBonusValue == 0) continue;

                Component line = buildSkillBonusLine(skill, skillBonusValue);
                header.add(line);
            }
            header.add(Component.literal(""));
        }

        return header;
    }

    private static Component buildSkillBonusLine(Skill skill, int value) {
        boolean isGood = (value > 0);

        MutableComponent skillBonusLine = Component.literal(StringUtils.toSignedString(value))
                .withStyle(Style.EMPTY.withColor(isGood ? ChatFormatting.GREEN : ChatFormatting.RED));
        skillBonusLine.append(Component.literal(" " + skill.getDisplayName()).withStyle(ChatFormatting.GRAY));

        return skillBonusLine;
    }

    private static MutableComponent buildRequirementLine(String requirementName, boolean fulfilled) {
        MutableComponent requirement;

        requirement = fulfilled
                ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                : Component.literal("✖ ").withStyle(ChatFormatting.RED);
        requirement.append(Component.literal(requirementName).withStyle(ChatFormatting.GRAY));
        return requirement;
    }
}
