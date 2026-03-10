/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class GearTooltipComponent extends IdentifiableTooltipComponent<GearInfo, GearInstance> {
    private static final Integer ELEMENTAL_DEFENSES_WIDTH = 40;

    @Override
    public List<Component> buildHeaderTooltip(GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        List<Component> header = new ArrayList<>();

        header.add(Component.empty());

        // FIXME: Add unid indicator
        // name
        MutableComponent nameLine = Component.empty().withStyle(ChatFormatting.WHITE);

        // spacing
        nameLine.append(Component.literal("\uDAFF\uDFF0").withStyle(SPACING_STYLE));

        String frameCode = gearInfo.type().getFrameCode();
        String spriteCode = gearInfo.type().getFrameSpriteCode();
        String setName = gearInfo.setInfo().map(SetInfo::name).orElse("");

        if (!setName.isEmpty()) {
            frameCode = String.valueOf((char) (frameCode.charAt(0) + 0x1000));
            spriteCode = String.valueOf((char) (spriteCode.charAt(0) + 0x1000));
        }

        nameLine.append(Component.literal(frameCode)
                .withStyle(Style.EMPTY
                        .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/frame")))
                        .withoutShadow()));

        // spacing
        nameLine.append(Component.literal("\uDAFF\uDFCF").withStyle(SPACING_STYLE));

        // frame sprite
        nameLine.append(Component.literal(spriteCode)
                .withStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/sprite"))))
                .withColor(0x00eb1c)
                .withoutShadow());

        // spacing
        nameLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));

        boolean isShiny = gearInstance != null && gearInstance.shinyStat().isPresent();
        String itemName = isShiny ? "Shiny " + gearInfo.name() : gearInfo.name();

        // item name
        nameLine.append(Component.literal(itemName)
                .withStyle(Style.EMPTY
                        .withFont(WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(gearInfo.tier().getChatFormatting())));

        header.add(nameLine);

        MutableComponent rarityTypeLine = Component.empty().withStyle(ChatFormatting.WHITE);

        rarityTypeLine.append(Component.literal("\uDB00\uDC23").withStyle(SPACING_STYLE));

        rarityTypeLine.append(BannerBoxFont.buildMessage(
                gearInfo.tier().getName(),
                CustomColor.fromChatFormatting(gearInfo.tier().getChatFormatting()),
                CommonColors.BLACK,
                "\uDB00\uDC02"));

        rarityTypeLine.append(Component.literal("\uDB00\uDC01").withStyle(SPACING_STYLE));

        boolean untradable = gearInfo.metaInfo().restrictions() == GearRestrictions.UNTRADABLE;
        CustomColor secondaryTierColor = getSecondaryTierColor(gearInfo.tier());

        rarityTypeLine.append(BannerBoxFont.buildMessage(
                gearInfo.type().name(), secondaryTierColor, CommonColors.BLACK, untradable ? "\uDB00\uDC02" : ""));

        if (untradable) {
            rarityTypeLine.append(Component.literal("\uDB00\uDC01").withStyle(SPACING_STYLE));

            rarityTypeLine.append(
                    Component.literal("\uE002").withStyle(RESTRICTION_STYLE).withColor(0xff4242));
            rarityTypeLine.append(Component.literal("\uDAFF\uDFF6\uF002").withStyle(RESTRICTION_STYLE));
        }

        header.add(rarityTypeLine);

        if (!setName.isBlank()) {
            MutableComponent setBonusLine = Component.empty().withColor(secondaryTierColor.asInt());

            setBonusLine.append(Component.literal("\uDB00\uDC23").withStyle(SPACING_STYLE));

            setBonusLine.append(
                    BannerBoxFont.buildMessage(setName + " set", secondaryTierColor, CommonColors.BLACK, ""));

            header.add(setBonusLine);
        }

        header.add(Component.empty());

        // Health for armor & accessories
        if (gearInfo.type().isArmor() || gearInfo.type().isAccessory()) {
            MutableComponent healthLine = Component.empty().withStyle(ChatFormatting.WHITE);

            healthLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));

            healthLine.append(Component.literal(StringUtils.toSignedCommaString(
                            gearInfo.fixedStats().healthBuff()))
                    .withStyle(Style.EMPTY
                            .withFont(new FontDescription.Resource(
                                    Identifier.withDefaultNamespace("offset/wynncraft_quad/12")))
                            .withColor(secondaryTierColor.asInt())));

            healthLine.append(Component.literal(" Health").withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT)));

            header.add(healthLine);
        } else if (gearInfo.type().isWeapon()) { // DPS for weapons
            MutableComponent dpsLine = Component.empty().withStyle(ChatFormatting.WHITE);

            dpsLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));

            dpsLine.append(
                    Component.literal(String.format("%,d", gearInfo.fixedStats().averageDps()))
                            .withStyle(Style.EMPTY
                                    .withFont(new FontDescription.Resource(
                                            Identifier.withDefaultNamespace("offset/wynncraft_quad/12")))
                                    .withColor(secondaryTierColor.asInt())));

            dpsLine.append(Component.literal(" DPS").withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT)));

            header.add(dpsLine);
        }

        // attack speed
        if (gearInfo.fixedStats().attackSpeed().isPresent()) {
            MutableComponent attackSpeedLine = Component.empty().withStyle(ChatFormatting.WHITE);

            attackSpeedLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));

            attackSpeedLine.append(Component.literal("\uE007")
                    .withStyle(Style.EMPTY
                            .withFont(new FontDescription.Resource(
                                    Identifier.withDefaultNamespace("tooltip/attribute/sprite")))
                            .withoutShadow()));

            attackSpeedLine.append(Component.literal(
                            " " + gearInfo.fixedStats().attackSpeed().get().getName() + " ")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));

            attackSpeedLine.append(Component.literal(
                            "(" + gearInfo.fixedStats().attackSpeed().get().getHitsPerSecond() + " hits/s)")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.DARK_GRAY)));

            header.add(attackSpeedLine);
        }

        List<Pair<Element, Integer>> defenses = gearInfo.fixedStats().defences();

        if (!defenses.isEmpty()) {
            MutableComponent defensesHeader = Component.empty().withStyle(ChatFormatting.WHITE);

            defensesHeader.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));
            defensesHeader.append(Component.literal("Elemental Defences")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));
            header.add(defensesHeader);

            MutableComponent defenseLine = Component.empty().withStyle(ChatFormatting.WHITE);

            for (int i = 0; i < defenses.size(); i++) {
                if (i == 0 || i == 3) {
                    defenseLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));
                }

                Pair<Element, Integer> defenseStat = defenses.get(i);

                MutableComponent elementComponent = Component.empty();
                String elementSymbol = defenseStat.a() == Element.EARTH
                        ? "\uDAFF\uDFFF" + defenseStat.a().getTooltipSprite()
                        : defenseStat.a().getTooltipSprite();

                if (defenseStat.a() == Element.EARTH
                        || defenseStat.a() == Element.THUNDER
                        || defenseStat.a() == Element.AIR) {
                    elementSymbol += "\uDAFF\uDFFF";
                }

                elementComponent.append(Component.literal(elementSymbol)
                        .withStyle(Style.EMPTY
                                .withFont(new FontDescription.Resource(
                                        Identifier.withDefaultNamespace("tooltip/attribute/sprite")))
                                .withColor(ChatFormatting.WHITE))
                        .withoutShadow());
                elementComponent.append(Component.literal(" ").withStyle(ChatFormatting.WHITE));
                elementComponent.append(Component.literal(StringUtils.toSignedCommaString(defenseStat.b()))
                        .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));

                // Doesn't match vanilla perfectly but is close enough
                int width = McUtils.mc().font.width(elementComponent);
                if (width < ELEMENTAL_DEFENSES_WIDTH) {
                    String offset = Managers.Font.calculateOffset(width, ELEMENTAL_DEFENSES_WIDTH);
                    elementComponent.append(
                            Component.literal(offset).withStyle(SPACING_STYLE).withStyle(ChatFormatting.GRAY));
                }

                boolean lastInRow = i == 2 || i == defenses.size() - 1;
                if (!lastInRow) {
                    elementComponent.append(Component.literal(" ")
                            .withStyle(Style.EMPTY
                                    .withFont(WYNNCRAFT_LANGUAGE_FONT)
                                    .withColor(ChatFormatting.GRAY)));
                }

                defenseLine.append(elementComponent);

                // 3 elements per line max
                if (i == defenses.size() - 1 || i == 2) {
                    header.add(defenseLine);
                    defenseLine = Component.empty().withStyle(ChatFormatting.WHITE);
                }
            }
        }

        // elemental damages
        List<Pair<DamageType, RangedValue>> damages = gearInfo.fixedStats().damages();

        if (!damages.isEmpty()) {
            MutableComponent damageLine = Component.empty().withStyle(ChatFormatting.WHITE);

            for (int i = 0; i < damages.size(); i++) {
                if (i == 0 || i == 3) {
                    damageLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));
                }

                Pair<DamageType, RangedValue> damageStat = damages.get(i);
                Element element = damageStat.a().getElement().orElse(null);

                MutableComponent elementComponent = Component.empty();
                String elementSymbol = element == Element.EARTH || damageStat.a() == DamageType.NEUTRAL
                        ? "\uDAFF\uDFFF" + damageStat.a().getTooltipSprite()
                        : damageStat.a().getTooltipSprite();

                if (damageStat.a() == DamageType.NEUTRAL
                        || element == Element.EARTH
                        || element == Element.THUNDER
                        || element == Element.AIR) {
                    elementSymbol += "\uDAFF\uDFFF";
                }

                elementComponent.append(Component.literal(elementSymbol)
                        .withStyle(Style.EMPTY
                                .withFont(new FontDescription.Resource(
                                        Identifier.withDefaultNamespace("tooltip/attribute/sprite")))
                                .withColor(ChatFormatting.WHITE))
                        .withoutShadow());
                elementComponent.append(Component.literal(" ").withStyle(ChatFormatting.WHITE));
                elementComponent.append(Component.literal(damageStat.b().asString())
                        .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));

                boolean lastInRow = i == 2 || i == damages.size() - 1;
                if (!lastInRow) {
                    elementComponent.append(Component.literal(" ").withStyle(ChatFormatting.WHITE));
                }

                damageLine.append(elementComponent);

                // 3 elements per line max
                if (i == damages.size() - 1 || i == 2) {
                    header.add(damageLine);
                    damageLine = Component.empty().withStyle(ChatFormatting.WHITE);
                }
            }
        }

        GearRequirements gearRequirements = gearInfo.requirements();

        if (!gearRequirements.skills().isEmpty()) {
            header.add(Component.empty());

            // Icons
            MutableComponent skillIconsLine =
                    Component.empty().withStyle(ChatFormatting.WHITE).withoutShadow();
            // TODO: Add spacer
            for (Pair<Skill, Integer> skill : gearRequirements.skills()) {
                skillIconsLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));

                String frame = skill.b() == 0 ? SKILL_REQ_FRAME_NONE : getSkillReqFrame(gearInfo.tier());

                skillIconsLine.append(Component.literal(frame).withStyle(SKILL_FRAME_STYLE));
                skillIconsLine.append(Component.literal("\uDAFF\uDFE7").withStyle(SPACING_STYLE));

                skillIconsLine.append(
                        Component.literal(getSkillReqIcon(skill.a().ordinal(), skill.b() != 0))
                                .withStyle(SKILL_SPRITE_STYLE));

                skillIconsLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));
            }

            header.add(skillIconsLine);

            header.add(Component.empty());

            // Counts
            MutableComponent skillCountLine = Component.empty().withStyle(ChatFormatting.WHITE);
            // TODO: Add spacer
            for (Pair<Skill, Integer> skill : gearRequirements.skills()) {
                int count = gearRequirements.skills().stream()
                        .filter(skillPair -> skillPair.a() == skill.a())
                        .map(Pair::b)
                        .findFirst()
                        .orElse(0);

                String reqCharacter = "\uE005";

                if (count != 0) {
                    reqCharacter = "\uE006";
                }

                skillCountLine.append(
                        Component.literal(reqCharacter + "\uDAFF\uDFFF").withStyle(REQUIREMENT_STYLE));
                skillCountLine.append(Component.literal("\uDB00\uDC03").withStyle(SPACING_STYLE));

                CustomColor color = CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY);

                if (count != 0) {
                    color = CustomColor.fromInt(0xacfac6);
                }

                skillCountLine.append(Component.literal(String.valueOf(count))
                        .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(color.asInt())));

                // TODO: Add spacer
            }

            header.add(skillCountLine);

            header.add(Component.empty());
        }

        if (gearRequirements.quest().isPresent()) {
            String questReq = gearRequirements.quest().get();
            MutableComponent questReqLine = Component.literal(" Quest")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.WHITE));

            // TODO: Calculate space based on width

            // TODO: Limit the quest name to 20 characters, replace stuff in the middle with "..."
            questReqLine.append(Component.literal(questReq + " ")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));
            questReqLine.append(Component.literal("(Lv. " + 1 + ")")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.DARK_GRAY)));

            header.add(buildRequirementLine(questReqLine, true));
        }

        if (gearRequirements.classType().isPresent()) {
            ClassType classType = gearRequirements.classType().get();

            MutableComponent classReqLine = Component.literal(" Class Type")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.WHITE));

            // TODO: Calculate space based on width

            classReqLine.append(Component.literal(classType.getFullName())
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));

            header.add(buildRequirementLine(classReqLine, true));
        }

        int level = gearRequirements.level();
        if (level != 0) {
            MutableComponent levelReqLine = Component.literal(" Combat Level")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.WHITE));

            // TODO: Calculate space based on width

            levelReqLine.append(Component.literal(String.valueOf(level))
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));

            header.add(buildRequirementLine(levelReqLine, true));
        }

        if (gearInstance != null && gearInstance.shinyStat().isPresent()) {
            ShinyStat shinyStat = gearInstance.shinyStat().get();
            MutableComponent shinyComponent = Component.empty().withStyle(ChatFormatting.WHITE);

            shinyComponent.append(Component.literal("\uE04F")
                    .withStyle(Style.EMPTY
                            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("common")))
                            .withoutShadow()));
            shinyComponent.append(Component.literal("\uDAFF\uDFFF").withStyle(SPACING_STYLE));

            shinyComponent.append(Component.literal(" " + shinyStat.statType().displayName())
                    .withStyle(Style.EMPTY
                            .withFont(WYNNCRAFT_LANGUAGE_FONT)
                            .withColor(getDividerColor(gearInfo.tier()).asInt())));

            // TODO: Add spacing

            shinyComponent.append(Component.literal(String.valueOf(shinyStat.value()))
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.WHITE)));

            header.add(shinyComponent);

            // TODO: Add rerolls
            String rerollExample =
                    "§f0§#c2f2f2ff \uE060\uDAFF\uDFFF\uE051\uDAFF\uDFFF\uE062\uDAFF\uDFFA§0\uE021\uDB00\uDC02§#c2f2f2ff\uDAFF\uDFFF\uE005§f\uDAFF\uDFF6\uF005";
        }

        return header;
    }

    public List<Component> buildFooterTooltip(GearInfo gearInfo, GearInstance gearInstance, boolean showItemType) {
        List<Component> footer = new ArrayList<>();

        return footer;
    }

    private static CustomColor getSecondaryTierColor(GearTier gearTier) {
        return switch (gearTier) {
            case NORMAL -> CustomColor.fromInt(0xe0e0e0);
            case UNIQUE -> CustomColor.fromInt(0xfff2b3);
            case RARE -> CustomColor.fromInt(0xf2c2f2);
            case LEGENDARY -> CustomColor.fromInt(0xcff9f9);
            case FABLED -> CustomColor.fromInt(0xf2c2c2);
            case MYTHIC -> CustomColor.fromInt(0xe0b3e6);
            // Crafteds shouldn't be used here
            default -> CustomColor.NONE;
        };
    }
}
