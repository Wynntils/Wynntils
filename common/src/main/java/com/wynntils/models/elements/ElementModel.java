/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderSpecialTierInfo;
import com.wynntils.models.elements.type.PowderTierInfo;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class ElementModel extends Model {
    private final List<PowderTierInfo> allPowderTierInfo;

    private final List<PowderSpecialTierInfo> allPowderSpecialTierInfo;

    public ElementModel() {
        super(List.of());

        allPowderTierInfo = buildPowderTierInfo();
        allPowderSpecialTierInfo = buildPowderSpecialTierInfo();
    }

    public List<PowderTierInfo> getAllPowderTierInfo() {
        return allPowderTierInfo;
    }

    public PowderTierInfo getPowderTierInfo(Powder element, int tier) {
        return allPowderTierInfo.stream()
                .filter(p -> p.element() == element && p.tier() == tier)
                .findFirst()
                .orElse(null);
    }

    public List<PowderSpecialTierInfo> getAllPowderSpecialTierInfo() {
        return allPowderSpecialTierInfo;
    }

    public PowderSpecialTierInfo getPowderSpecialTierInfo(String name, int tier) {
        return allPowderSpecialTierInfo.stream()
                .filter(p -> p.name().equals(name) && p.tier() == tier)
                .findFirst()
                .orElse(null);
    }

    public PowderSpecialTierInfo getPowderSpecialTierInfo(int encodingId, int tier) {
        return allPowderSpecialTierInfo.stream()
                .filter(p -> p.encodingId() == encodingId && p.tier() == tier)
                .findFirst()
                .orElse(null);
    }

    public List<Element> getGearElementOrder() {
        return List.of(Element.EARTH, Element.THUNDER, Element.WATER, Element.FIRE, Element.AIR);
    }

    public static Element getOpposingElement(Element element) {
        return switch (element) {
            case EARTH -> Element.AIR;
            case THUNDER -> Element.EARTH;
            case WATER -> Element.THUNDER;
            case FIRE -> Element.WATER;
            case AIR -> Element.FIRE;
        };
    }

    public Powder getOpposingElement(Powder powder) {
        return Powder.fromElement(getOpposingElement(powder.getElement()));
    }

    private List<PowderTierInfo> buildPowderTierInfo() {
        return List.of(
                new PowderTierInfo(Powder.WATER, 1, 1, 3, 4, 13, 5, 3, 1),
                new PowderTierInfo(Powder.WATER, 2, 5, 5, 6, 15, 10, 6, 1),
                new PowderTierInfo(Powder.WATER, 3, 15, 6, 8, 17, 20, 11, 3),
                new PowderTierInfo(Powder.WATER, 4, 25, 7, 8, 21, 30, 16, 4),
                new PowderTierInfo(Powder.WATER, 5, 40, 8, 10, 26, 45, 23, 6),
                new PowderTierInfo(Powder.WATER, 6, 55, 10, 13, 32, 60, 32, 10),
                new PowderTierInfo(Powder.WATER, 7, 70, 11, 15, 38, 75, 40, 15),
                new PowderTierInfo(Powder.FIRE, 1, 1, 2, 5, 14, 5, 3, 1),
                new PowderTierInfo(Powder.FIRE, 2, 5, 4, 7, 16, 10, 6, 1),
                new PowderTierInfo(Powder.FIRE, 3, 15, 5, 9, 19, 20, 10, 2),
                new PowderTierInfo(Powder.FIRE, 4, 25, 6, 9, 24, 30, 15, 3),
                new PowderTierInfo(Powder.FIRE, 5, 40, 7, 11, 30, 45, 22, 5),
                new PowderTierInfo(Powder.FIRE, 6, 55, 9, 14, 37, 60, 31, 9),
                new PowderTierInfo(Powder.FIRE, 7, 70, 10, 16, 44, 75, 39, 14),
                new PowderTierInfo(Powder.AIR, 1, 1, 2, 6, 11, 5, 3, 1),
                new PowderTierInfo(Powder.AIR, 2, 5, 3, 9, 14, 10, 6, 2),
                new PowderTierInfo(Powder.AIR, 3, 15, 4, 11, 17, 20, 10, 3),
                new PowderTierInfo(Powder.AIR, 4, 25, 5, 11, 22, 30, 16, 5),
                new PowderTierInfo(Powder.AIR, 5, 40, 7, 12, 28, 45, 23, 7),
                new PowderTierInfo(Powder.AIR, 6, 55, 8, 15, 35, 60, 30, 8),
                new PowderTierInfo(Powder.AIR, 7, 70, 9, 17, 42, 75, 38, 13),
                new PowderTierInfo(Powder.EARTH, 1, 1, 4, 5, 17, 5, 2, 1),
                new PowderTierInfo(Powder.EARTH, 2, 5, 6, 7, 21, 10, 5, 2),
                new PowderTierInfo(Powder.EARTH, 3, 15, 7, 9, 25, 20, 9, 3),
                new PowderTierInfo(Powder.EARTH, 4, 25, 8, 9, 31, 30, 14, 4),
                new PowderTierInfo(Powder.EARTH, 5, 40, 9, 11, 38, 45, 22, 7),
                new PowderTierInfo(Powder.EARTH, 6, 55, 11, 12, 46, 60, 29, 7),
                new PowderTierInfo(Powder.EARTH, 7, 70, 12, 14, 52, 75, 37, 12),
                new PowderTierInfo(Powder.THUNDER, 1, 1, 1, 8, 9, 5, 2, 1),
                new PowderTierInfo(Powder.THUNDER, 2, 5, 1, 12, 11, 10, 4, 1),
                new PowderTierInfo(Powder.THUNDER, 3, 15, 2, 14, 13, 20, 8, 2),
                new PowderTierInfo(Powder.THUNDER, 4, 25, 2, 15, 17, 30, 13, 3),
                new PowderTierInfo(Powder.THUNDER, 5, 40, 3, 17, 22, 45, 20, 5),
                new PowderTierInfo(Powder.THUNDER, 6, 55, 4, 19, 28, 60, 28, 6),
                new PowderTierInfo(Powder.THUNDER, 7, 70, 5, 21, 32, 75, 36, 11));
    }

    private List<PowderSpecialTierInfo> buildPowderSpecialTierInfo() {
        Component quakeDescription = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal("Sunder the ground around you, damaging enemies within "))
                .append(Component.literal("%1$s blocks").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" for "))
                .append(Component.literal("%2$s% Main Attack Damage").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal("."));
        Component rageDescription = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal("Increase your "))
                .append(Component.literal("\uE001")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(ChatFormatting.DARK_GREEN)))
                .append(Component.literal(" Earth Damage").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN)))
                .append(Component.literal(" by"))
                .append(Component.literal(" %1$s%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" for every"))
                .append(Component.literal(" 1%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" of your health missing under 75%"));
        Component chainLightningDescription = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal("Unleash a bolt of lightning at the nearest enemy within "))
                .append(Component.literal("14 blocks").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(", chaining up to "))
                .append(Component.literal("%1$s").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" times and dealing "))
                .append(Component.literal("%2$s% Main Attack Damage").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal("."));
        Component killStreakDescription = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal("Increase your "))
                .append(Component.literal("\uE003")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(ChatFormatting.YELLOW)))
                .append(Component.literal(" Thunder Damage").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)))
                .append(Component.literal(" by "))
                .append(Component.literal("%1$s%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" for "))
                .append(Component.literal("10 seconds").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" upon killing an enemy, up to "))
                .append(Component.literal("200%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT));
        Component curseDescription = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal("Hex enemies within "))
                .append(Component.literal("8 blocks").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(", increasing damage they take by "))
                .append(Component.literal("%1$s%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" for "))
                .append(Component.literal("4 seconds").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal("."));
        Component concentrationDescription = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal("Increase your "))
                .append(Component.literal("\uE004")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(ChatFormatting.AQUA)))
                .append(Component.literal(" Water Damage").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)))
                .append(Component.literal(" by "))
                .append(Component.literal("%1$s%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" for "))
                .append(Component.literal("1 second").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" when using mana, scaling damage and duration per mana spent, up to "))
                .append(Component.literal("120%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT));
        Component courageDescription = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal("Baptize everything within "))
                .append(Component.literal("5 blocks").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" in fire, scorching enemies for "))
                .append(Component.literal("%1$s% Main Attack Damage").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" and granting players a "))
                .append(Component.literal("+%2$s% damage").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" bonus for the next "))
                .append(Component.literal("4 seconds").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal("."));
        Component enduranceDescription = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal("Increase your "))
                .append(Component.literal("\uE002")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(ChatFormatting.RED)))
                .append(Component.literal(" Fire Damage").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)))
                .append(Component.literal(" by "))
                .append(Component.literal("%1$s%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" for "))
                .append(Component.literal("8 seconds").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" when you take damage, up to "))
                .append(Component.literal("120%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT));
        Component windPrisonDescription = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal("Ensnare enemies within "))
                .append(Component.literal("8 blocks").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" for "))
                .append(Component.literal("5 seconds").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(". The next hit they take will deal "))
                .append(Component.literal("+%1$s% damage").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" and set them free."));
        Component dodgeDescription = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal("Increase your "))
                .append(Component.literal("\uE000")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(ChatFormatting.WHITE)))
                .append(Component.literal(" Air Damage").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))
                .append(Component.literal(" by "))
                .append(Component.literal("%1$s%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" per second spent within "))
                .append(Component.literal("5 blocks").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" of an enemy without getting hit, up to "))
                .append(Component.literal("120%").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT));
        return List.of(
                new PowderSpecialTierInfo(Powder.EARTH, 1, 1, "Quake", true, 4.5, 240.0, quakeDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 2, 1, "Quake", true, 5.0, 280.0, quakeDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 3, 1, "Quake", true, 5.5, 320.0, quakeDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 4, 1, "Quake", true, 6.0, 360.0, quakeDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 5, 1, "Quake", true, 6.5, 400.0, quakeDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 6, 1, "Quake", true, 7.0, 440.0, quakeDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 7, 1, "Quake", true, 7.5, 480.0, quakeDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 1, 2, "Rage", false, 0.4, null, rageDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 2, 2, "Rage", false, 0.5, null, rageDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 3, 2, "Rage", false, 0.6, null, rageDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 4, 2, "Rage", false, 0.7, null, rageDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 5, 2, "Rage", false, 0.8, null, rageDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 6, 2, "Rage", false, 0.9, null, rageDescription),
                new PowderSpecialTierInfo(Powder.EARTH, 7, 2, "Rage", false, 1.0, null, rageDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 1, 3, "Chain Lightning", true, 5.0, 200.0, chainLightningDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 2, 3, "Chain Lightning", true, 6.0, 225.0, chainLightningDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 3, 3, "Chain Lightning", true, 7.0, 250.0, chainLightningDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 4, 3, "Chain Lightning", true, 8.0, 275.0, chainLightningDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 5, 3, "Chain Lightning", true, 9.0, 300.0, chainLightningDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 6, 3, "Chain Lightning", true, 10.0, 325.0, chainLightningDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 7, 3, "Chain Lightning", true, 11.0, 350.0, chainLightningDescription),
                new PowderSpecialTierInfo(Powder.THUNDER, 1, 4, "Kill Streak", false, 6.0, null, killStreakDescription),
                new PowderSpecialTierInfo(Powder.THUNDER, 2, 4, "Kill Streak", false, 7.5, null, killStreakDescription),
                new PowderSpecialTierInfo(Powder.THUNDER, 3, 4, "Kill Streak", false, 9.0, null, killStreakDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 4, 4, "Kill Streak", false, 10.5, null, killStreakDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 5, 4, "Kill Streak", false, 12.0, null, killStreakDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 6, 4, "Kill Streak", false, 13.5, null, killStreakDescription),
                new PowderSpecialTierInfo(
                        Powder.THUNDER, 7, 4, "Kill Streak", false, 15.0, null, killStreakDescription),
                new PowderSpecialTierInfo(Powder.WATER, 1, 5, "Curse", true, 10.0, null, curseDescription),
                new PowderSpecialTierInfo(Powder.WATER, 2, 5, "Curse", true, 12.5, null, curseDescription),
                new PowderSpecialTierInfo(Powder.WATER, 3, 5, "Curse", true, 15.0, null, curseDescription),
                new PowderSpecialTierInfo(Powder.WATER, 4, 5, "Curse", true, 17.5, null, curseDescription),
                new PowderSpecialTierInfo(Powder.WATER, 5, 5, "Curse", true, 20.0, null, curseDescription),
                new PowderSpecialTierInfo(Powder.WATER, 6, 5, "Curse", true, 22.5, null, curseDescription),
                new PowderSpecialTierInfo(Powder.WATER, 7, 5, "Curse", true, 25.0, null, curseDescription),
                new PowderSpecialTierInfo(
                        Powder.WATER, 1, 6, "Concentration", false, 0.05, null, concentrationDescription),
                new PowderSpecialTierInfo(
                        Powder.WATER, 2, 6, "Concentration", false, 0.075, null, concentrationDescription),
                new PowderSpecialTierInfo(
                        Powder.WATER, 3, 6, "Concentration", false, 0.1, null, concentrationDescription),
                new PowderSpecialTierInfo(
                        Powder.WATER, 4, 6, "Concentration", false, 0.125, null, concentrationDescription),
                new PowderSpecialTierInfo(
                        Powder.WATER, 5, 6, "Concentration", false, 0.15, null, concentrationDescription),
                new PowderSpecialTierInfo(
                        Powder.WATER, 6, 6, "Concentration", false, 0.175, null, concentrationDescription),
                new PowderSpecialTierInfo(
                        Powder.WATER, 7, 6, "Concentration", false, 0.2, null, concentrationDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 1, 7, "Courage", true, 110.0, 10.0, courageDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 2, 7, "Courage", true, 125.0, 12.5, courageDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 3, 7, "Courage", true, 140.0, 15.0, courageDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 4, 7, "Courage", true, 155.0, 17.5, courageDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 5, 7, "Courage", true, 170.0, 20.0, courageDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 6, 7, "Courage", true, 185.0, 22.5, courageDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 7, 7, "Courage", true, 200.0, 25.0, courageDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 1, 8, "Endurance", false, 2.0, null, enduranceDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 2, 8, "Endurance", false, 3.0, null, enduranceDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 3, 8, "Endurance", false, 4.0, null, enduranceDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 4, 8, "Endurance", false, 5.0, null, enduranceDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 5, 8, "Endurance", false, 6.0, null, enduranceDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 6, 8, "Endurance", false, 7.0, null, enduranceDescription),
                new PowderSpecialTierInfo(Powder.FIRE, 7, 8, "Endurance", false, 8.0, null, enduranceDescription),
                new PowderSpecialTierInfo(Powder.AIR, 1, 9, "Wind Prison", true, 100.0, null, windPrisonDescription),
                new PowderSpecialTierInfo(Powder.AIR, 2, 9, "Wind Prison", true, 125.0, null, windPrisonDescription),
                new PowderSpecialTierInfo(Powder.AIR, 3, 9, "Wind Prison", true, 150.0, null, windPrisonDescription),
                new PowderSpecialTierInfo(Powder.AIR, 4, 9, "Wind Prison", true, 175.0, null, windPrisonDescription),
                new PowderSpecialTierInfo(Powder.AIR, 5, 9, "Wind Prison", true, 200.0, null, windPrisonDescription),
                new PowderSpecialTierInfo(Powder.AIR, 6, 9, "Wind Prison", true, 225.0, null, windPrisonDescription),
                new PowderSpecialTierInfo(Powder.AIR, 7, 9, "Wind Prison", true, 250.0, null, windPrisonDescription),
                new PowderSpecialTierInfo(Powder.AIR, 1, 10, "Dodge", false, 2.0, null, dodgeDescription),
                new PowderSpecialTierInfo(Powder.AIR, 2, 10, "Dodge", false, 3.0, null, dodgeDescription),
                new PowderSpecialTierInfo(Powder.AIR, 3, 10, "Dodge", false, 4.0, null, dodgeDescription),
                new PowderSpecialTierInfo(Powder.AIR, 4, 10, "Dodge", false, 5.0, null, dodgeDescription),
                new PowderSpecialTierInfo(Powder.AIR, 5, 10, "Dodge", false, 6.0, null, dodgeDescription),
                new PowderSpecialTierInfo(Powder.AIR, 6, 10, "Dodge", false, 7.0, null, dodgeDescription),
                new PowderSpecialTierInfo(Powder.AIR, 7, 10, "Dodge", false, 8.0, null, dodgeDescription));
    }
}
