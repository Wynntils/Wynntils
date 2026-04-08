/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components.gear;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivitySortOrder;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

final class GearTooltipSupport {
    static final int ELEMENTAL_DEFENSES_WIDTH = 40;
    static final Style WYNNCRAFT_WHITE_STYLE = Style.EMPTY
            .withFont(
                    com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                            .WYNNCRAFT_LANGUAGE_FONT)
            .withColor(ChatFormatting.WHITE);
    static final FontDescription EMBLEM_FRAME_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/frame"));
    static final FontDescription EMBLEM_SPRITE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/sprite"));
    static final FontDescription ATTRIBUTE_SPRITE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/attribute/sprite"));
    static final FontDescription TOOLTIP_BANNER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/banner"));
    static final FontDescription COMMON_FONT = new FontDescription.Resource(Identifier.withDefaultNamespace("common"));

    private static final Pattern QUEST_NAME_SANITIZER = Pattern.compile("[^a-z0-9]+");
    private static final char SKILL_REQ_FRAME_BASE = '\uE000';
    private static final char SKILL_REQ_ICON_BASE_ACTIVE = '\uE000';
    private static final char SKILL_REQ_ICON_BASE_UNUSED = '\uE010';

    private GearTooltipSupport() {}

    static String getSkillReqFrame(GearTier gearTier) {
        GearTier[] valid = GearTier.validValues();
        for (int i = 0; i < valid.length; i++) {
            if (valid[i] == gearTier) {
                return String.valueOf((char) (SKILL_REQ_FRAME_BASE + i));
            }
        }

        return com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent.SKILL_REQ_FRAME_NONE;
    }

    static String getSkillReqIcon(int ordinal, boolean active) {
        return String.valueOf((char) ((active ? SKILL_REQ_ICON_BASE_ACTIVE : SKILL_REQ_ICON_BASE_UNUSED) + ordinal));
    }

    static MutableComponent withWhiteShadow(Component component) {
        return Component.empty()
                .withStyle(style -> style.withShadowColor(0xFFFFFF))
                .append(component.copy());
    }

    static CustomColor getSecondaryTierColor(GearTier gearTier) {
        return switch (gearTier) {
            case NORMAL -> CustomColor.fromInt(0xe0e0e0);
            case UNIQUE -> CustomColor.fromInt(0xfff2b3);
            case RARE -> CustomColor.fromInt(0xf2c2f2);
            case LEGENDARY -> CustomColor.fromInt(0xcff9f9);
            case FABLED -> CustomColor.fromInt(0xf2c2c2);
            case MYTHIC -> CustomColor.fromInt(0xe0b3e6);
            default -> CustomColor.NONE;
        };
    }

    static CustomColor getDividerColor(GearTier gearTier) {
        return switch (gearTier) {
            case NORMAL -> CustomColor.fromInt(0xe0e0e0);
            case UNIQUE -> CustomColor.fromInt(0xfff2b3);
            case RARE -> CustomColor.fromInt(0xf2c2f2);
            case LEGENDARY -> CustomColor.fromInt(0xc2f2f2);
            case FABLED -> CustomColor.fromInt(0xf2c2c2);
            case MYTHIC -> CustomColor.fromInt(0xe0b3e6);
            default -> CustomColor.NONE;
        };
    }

    static int getDisplayedDps(GearInfo gearInfo) {
        int averageDps = gearInfo.fixedStats().averageDps();
        if (averageDps > 0) {
            return averageDps;
        }

        if (gearInfo.fixedStats().attackSpeed().isEmpty()
                || gearInfo.fixedStats().damages().isEmpty()) {
            return averageDps;
        }

        double averageDamage = 0;
        for (Pair<DamageType, RangedValue> damageStat : gearInfo.fixedStats().damages()) {
            averageDamage += (damageStat.b().low() + damageStat.b().high()) / 2.0;
        }

        return (int) Math.round(
                averageDamage * gearInfo.fixedStats().attackSpeed().get().getHitsPerSecond());
    }

    static MutableComponent buildRequirementValueLine(Component label, Component value, boolean fulfilled) {
        MutableComponent requirement = Component.empty();
        requirement.append(withWhiteShadow(
                fulfilled
                        ? Component.literal("\uE006\uDAFF\uDFFF")
                                .withStyle(
                                        com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                                .REQUIREMENT_STYLE)
                        : Component.literal("\uE007\uDAFF\uDFFF")
                                .withStyle(
                                        com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                                .REQUIREMENT_STYLE)));
        requirement.append(label.copy());
        requirement.append(value.copy());
        return requirement;
    }

    static MutableComponent buildShinyStatLine(ShinyStat shinyStat, GearTier gearTier) {
        MutableComponent left = Component.empty().withStyle(WYNNCRAFT_WHITE_STYLE);
        left.append(withWhiteShadow(Component.literal("\uE04F").withStyle(Style.EMPTY.withFont(COMMON_FONT))));
        left.append(Component.literal("\uDAFF\uDFFF")
                .withStyle(com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent.SPACING_STYLE));
        left.append(Component.literal(" " + shinyStat.statType().displayName())
                .withStyle(Style.EMPTY
                        .withFont(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(getDividerColor(gearTier).asInt())));

        MutableComponent right = Component.literal(String.valueOf(shinyStat.value()))
                .withStyle(Style.EMPTY
                        .withFont(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(ChatFormatting.WHITE));

        return Component.empty().append(left).append(right);
    }

    static Optional<QuestInfo> resolveQuestInfo(String questRequirementName) {
        Optional<QuestInfo> exactMatch = Models.Quest.getQuestInfoFromName(questRequirementName);
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        String normalizedRequirement = normalizeQuestName(questRequirementName);
        if (normalizedRequirement.isEmpty()) {
            return Optional.empty();
        }

        return Models.Quest.getSortedQuests(ActivitySortOrder.ALPHABETIC, true, true).stream()
                .filter(questInfo -> normalizeQuestName(questInfo.name()).equals(normalizedRequirement))
                .findFirst();
    }

    private static String normalizeQuestName(String questName) {
        String normalized = questName
                .replace("â€™", "'")
                .replace('`', '\'')
                .replace("&#039;", "'")
                .trim();

        if (normalized.startsWith("Mini-Quest - ")) {
            normalized = normalized.substring("Mini-Quest - ".length());
        } else if (normalized.startsWith("Mini-Quest: ")) {
            normalized = normalized.substring("Mini-Quest: ".length());
        } else if (normalized.startsWith("Mini Quest - ")) {
            normalized = normalized.substring("Mini Quest - ".length());
        }

        normalized = normalized.toLowerCase(Locale.ROOT);
        return QUEST_NAME_SANITIZER.matcher(normalized).replaceAll(" ").trim();
    }

    static void appendOffset(MutableComponent line, int pixels) {
        if (pixels <= 0) {
            return;
        }

        String offset = Managers.Font.calculateOffset(0, pixels);
        if (!offset.isEmpty()) {
            line.append(Component.literal(offset)
                    .withStyle(
                            com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                    .SPACING_STYLE));
        }
    }
}
