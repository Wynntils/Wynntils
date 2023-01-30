/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.tooltip;

import com.wynntils.models.concepts.Element;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.gearinfo.type.GearRequirements;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.StatUnit;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class GearTooltipHeader {
    public static List<Component> buildTooltip(GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        List<Component> header = new ArrayList<>();

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

        // FIXME: Is requirements in the correct order? Also add checks if the requirements
        // are fulfilled...
        // requirements
        GearRequirements requirements = gearInfo.requirements();
        if (requirements.quest().isPresent()) {
            header.add(getRequirement("Quest Req: " + requirements.quest().get()));
        }
        if (requirements.classType().isPresent()) {
            header.add(getRequirement(
                    "Class Req: " + requirements.classType().get().getFullName()));
        }
        if (requirements.level() != 0) {
            header.add(getRequirement("Combat Lv. Min: " + requirements.level()));
        }
        if (!requirements.skills().isEmpty()) {
            for (Pair<Skill, Integer> skillRequirement : requirements.skills()) {
                header.add(
                        getRequirement(skillRequirement.key().getDisplayName() + " Min: " + skillRequirement.value()));
            }
        }

        // FIXME: Only add if we had requirements
        header.add(Component.literal(""));

        // Add delimiter if variables stats will follow
        if (!gearInfo.variableStats().isEmpty()) {
            header.add(Component.literal(""));
        }

        appendSkillBonuses(gearInfo, header);

        return header;
    }

    // FIXME: This should really be part of PreVariable tooltip, but we need a better
    // split for that.
    private static void appendSkillBonuses(GearInfo gearInfo, List<Component> allStatLines) {
        List<Pair<Skill, Integer>> skillBonuses = gearInfo.fixedStats().skillBonuses();
        for (Skill skill : Skill.getGearSkillOrder()) {
            Pair<Skill, Integer> skillBonusValue = getSkillBonuses(skill, skillBonuses);
            if (skillBonusValue == null) continue;

            Component line = buildBaseComponent(
                    skillBonusValue.key().getDisplayName(), skillBonusValue.value(), StatUnit.RAW, false, "");
            allStatLines.add(line);
        }
        if (!skillBonuses.isEmpty()) {
            allStatLines.add(Component.literal(""));
        }
    }

    private static MutableComponent buildBaseComponent(
            String inGameName, int value, StatUnit unitType, boolean invert, String stars) {
        String unit = unitType.getDisplayName();

        MutableComponent baseComponent = Component.literal("");

        int valueToShow = invert ? -value : value;

        MutableComponent statInfo = Component.literal(StringUtils.toSignedString(valueToShow) + unit);
        boolean isGood = (value > 0);
        statInfo.setStyle(Style.EMPTY.withColor(isGood ? ChatFormatting.GREEN : ChatFormatting.RED));

        baseComponent.append(statInfo);

        if (!stars.isEmpty()) {
            baseComponent.append(Component.literal(stars).withStyle(ChatFormatting.DARK_GREEN));
        }

        baseComponent.append(Component.literal(" " + inGameName).withStyle(ChatFormatting.GRAY));

        return baseComponent;
    }

    private static Pair<Skill, Integer> getSkillBonuses(Skill skill, List<Pair<Skill, Integer>> skillBonuses) {
        for (Pair<Skill, Integer> skillBonusValue : skillBonuses) {
            if (skillBonusValue.key() == skill) {
                return skillBonusValue;
            }
        }

        return null;
    }

    private static MutableComponent getRequirement(String requirementName) {
        MutableComponent requirement;
        requirement = Component.literal("✔ ").withStyle(ChatFormatting.GREEN);
        requirement.append(Component.literal(requirementName).withStyle(ChatFormatting.GRAY));
        return requirement;
    }
}
