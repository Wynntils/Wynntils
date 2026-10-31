/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.TooltipBannerFont;
import com.wynntils.core.text.fonts.wynnfonts.TooltipIdentificationMeterFont;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderSpecialTierInfo;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearInstanceRequirements;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.type.ConsumableEffect;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.models.wynnitem.type.NamedItemEffect;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class WynnItemParser {
    // Test in WynnItemParser_BLACK_BANNER_TEXT_PATTERN
    private static final Pattern BLACK_BANNER_TEXT_PATTERN = Pattern.compile("§0([^§]*)");
    // Test in WynnItemParser_SET_PATTERN
    private static final Pattern SET_PATTERN = Pattern.compile(
            "§[5bcdef]\uE060\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE062\uDAFF\uDFEC§0\uE012\uE004\uE013\uDB00\uDC02.+?§7(?<equipped>\\d+)/(?<total>\\d+)");

    // Test in WynnItemParser_HEALTH_PATTERN
    private static final Pattern HEALTH_PATTERN =
            Pattern.compile("^(?:§f\uDB00\uDC02)?§#(?:[a-f0-9]{8})([+-][\\d,]+)§f Health$");

    // Test in WynnItemParser_DPS_PATTERN
    private static final Pattern DPS_PATTERN = Pattern.compile("^(?:§f\uDB00\uDC02)?§#(?:[a-f0-9]{8})([\\d,]+)§f DPS$");

    // Test in WynnItemParser_DURABILITY_PATTERN
    private static final Pattern DURABILITY_PATTERN =
            Pattern.compile("§8\uE023\uDAFF\uDFF7§#.{8}.§7 Durability (\\d+)\\/(\\d+)");

    // Test in WynnItemParser_CONSUMABLE_CHARGES_PATTERN
    private static final Pattern CONSUMABLE_CHARGES_PATTERN =
            Pattern.compile(".*?(?<current>\\d+)\\/(?<max>\\d+) Charges$");

    // Test in WynnItemParser_CONSUMABLE_DURATION_PATTERN
    private static final Pattern CONSUMABLE_DURATION_PATTERN =
            Pattern.compile(".*?(?<duration>(?:(?:\\d+h )?(?:\\d+m )?\\d+s)) Duration$");

    // Test in WynnItemParser_DISPLAY_DURATION_PATTERN
    private static final Pattern DISPLAY_DURATION_PATTERN =
            Pattern.compile("(?:(?<hours>\\d+)h )?(?:(?<minutes>\\d+)m )?(?<seconds>\\d+)s");

    // Test in WynnItemParser_ITEM_ATTACK_SPEED_PATTERN
    private static final Pattern ITEM_ATTACK_SPEED_PATTERN =
            Pattern.compile("^§f(?:\uDB00\uDC02)?\uE007§7 ([\\w ]+) §8\\((\\d+\\.\\d+) hits\\/s\\)$");

    // Test in WynnItemParser_ITEM_DAMAGE_PATTERN
    private static final Pattern ITEM_DAMAGE_PATTERN =
            Pattern.compile("§f(?:[^§]|§.)*?(?<spriteSymbol>[\uE000-\uE005])(?:[^§]|§.)*?§7(?<range>\\d+-\\d+)");

    private static final String ELEMENTAL_DEFENSES_LINE = "§7Elemental Defences";

    // Test in WynnItemParser_ITEM_DEFENCE_PATTERN
    private static final Pattern ITEM_DEFENCE_PATTERN =
            Pattern.compile("§f[^§]*?(?<spriteSymbol>[\uE000-\uE004])[^§]*?§7(?<value>[+-]?\\d+)");

    // Test in WynnItemParser_IDENTIFICATION_STAT_PATTERN
    private static final Pattern IDENTIFICATION_STAT_PATTERN = Pattern.compile(
            "§f[^§\\w]*(?<statName>[\\w\\.\\- ]+).+?§#(acfac6ff|faacacff)(?<value>[-+][\\d,]+)(?<unit>%| tier|\\/[35]s)?(?:§f §8[^§]+(?:§(?<indicatorColor>#[a-zA-Z0-9]{8})(?<vanillaMeter>[\uE000-\uE023]|\uDB00\uDC09)?)?)?");

    // Test in WynnItemParser_TIER_PATTERN
    private static final Pattern TIER_PATTERN = Pattern.compile("^(?:§.)*\uDB00\uDC26(?:§([5bcdef]))?.+");

    // Test in WynnItemParser_REROLL_PATTERN
    private static final Pattern REROLL_PATTERN = Pattern.compile(
            "§f[^§]+§#[0-9a-f]{8}(?<banner>\uE060(?:\uDAFF\uDFFF[\uE050-\uE059])+\uDAFF\uDFFF\uE062)[^§]+§0[\uE020-\uE029]\uDB00\uDC02§#[0-9a-f]{8}\uDAFF\uDFFF\uE005§f\uDAFF\uDFF6\uF005");

    // Test in WynnItemParser_REROLL_EXTRACT_PATTERN
    private static final Pattern REROLL_EXTRACT_PATTERN = Pattern.compile("\uE060(.*?)\uE062");

    // Test in WynnItemParser_MIN_LEVEL_PATTERN
    private static final Pattern MIN_LEVEL_PATTERN =
            Pattern.compile("^§f(\uE006|\uE007)\uDAFF\uDFFF Combat Level.+?§7(?<level>\\d+)$");

    // Test in WynnItemParser_CLASS_REQ_PATTERN
    private static final Pattern CLASS_REQ_PATTERN =
            Pattern.compile("^§f(\uE006|\uE007)\uDAFF\uDFFF Class Type.+?§7(?<name>.+)\\/(?<skinned>.+)$");

    private static final String SKILL_REQ_PART = ".+?(\uE005|\uE006|\uE007).+?§(?:8|#acfac6ff|#faacacff)(\\d+).+?";
    // Test in WynnItemParser_SKILL_REQ_PART_PATTERN
    private static final Pattern SKILL_REQ_PART_PATTERN = Pattern.compile(SKILL_REQ_PART);

    // Test in WynnItemParser_SKILL_REQ_PATTERN
    private static final Pattern SKILL_REQ_PATTERN = Pattern.compile("(?:" + SKILL_REQ_PART + "){5}");

    // Test in WynnItemParser_QUEST_REQ_PATTERN
    private static final Pattern QUEST_REQ_PATTERN =
            Pattern.compile("^§f(\uE006|\uE007)\uDAFF\uDFFF Quest.+?§7([^§]+) §8\\(Lv\\. (?<level>\\d+)\\)$");

    // Test in WynnItemParser_SHINY_STAT_PATTERN
    private static final Pattern SHINY_STAT_PATTERN = Pattern.compile(
            "^§f\uE04F\uDAFF\uDFFF§#(?:[a-f0-9]{8})(?: )? ([a-zA-Z ]+).+?§f([\\d,]+)§#(?:[a-f0-9]{8})(\uDB00\uDC00|.+)$");

    // Test in WynnItemParser_MAJOR_ID_HEADER_PATTERN
    private static final Pattern MAJOR_ID_HEADER_PATTERN =
            Pattern.compile("§#[0-9a-f]{8}\uE000\uDB00\uDC02[^§:]+: (?:§7.+)?");

    // Test in WynnItemParser_MAJOR_ID_DESCRIPTION_PATTERN
    private static final Pattern MAJOR_ID_DESCRIPTION_PATTERN =
            Pattern.compile("§7.+|§(?:[0-9a-f]|#[0-9a-f]{8}).+§7.*");

    // Test in WynnItemParser_LORE_PATTERN
    private static final Pattern LORE_PATTERN = Pattern.compile("§f.+§8§o?(?<lore>.+)");

    // Test in WynnItemParser_LORE_PAGE_PATTERN
    private static final Pattern LORE_PAGE_PATTERN = Pattern.compile("(?:§f.*?)?(?<lore>§7(?:§o)?.+)");

    // Test in WynnItemParser_COMMUNITY_EVENT_WINNER_PATTERN
    private static final Pattern COMMUNITY_EVENT_WINNER_PATTERN = Pattern.compile(".*?(?<lore>§7"
            + Pattern.quote("󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏾂")
            + "§0" + Pattern.quote("  ") + "(?:\uDB00\uDC02)?)");

    // Test in WynnItemParser_CRAFTED_BY_PATTERN
    private static final Pattern CRAFTED_BY_PATTERN = Pattern.compile("§f.+§#aed4d4ffCrafted by ?(?<creator>.+)");

    // Test in WynnItemParser_POWDER_TITLE_PATTERN
    private static final Pattern POWDER_TITLE_PATTERN = Pattern.compile(
            "§#[0-9a-fA-F]{8}\uE060\uDAFF\uDFFF\uE03F\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE046\uDAFF\uDFFF\uE033\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE03A\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE062\uDAFF\uDFAC§0\uE00F\uE00E\uE016\uE003\uE004\uE011 \uE012\uE00E\uE002\uE00A\uE004\uE013\uE012\uDB00\uDC02§f.+§7(?<applied>\\d+)/(?<slots>\\d+)");

    // Test in WynnItemParser_POWDER_PATTERN
    private static final Pattern POWDER_PATTERN = Pattern.compile(
            "§f.\uDAFF\uDFFF\\s*(?:§[2ebcf])?(?<powderOne>Earth|Thunder|Water|Fire|Air) (?<powderOneTier>[IV]{1,3})[^§\\sA-Za-z]*(?:\\s+(?:§f)?.\uDAFF\uDFFF\\s*(?:§[2ebcf])?(?<powderTwo>Earth|Thunder|Water|Fire|Air) (?<powderTwoTier>[IV]{1,3})[^§\\sA-Za-z]*)?");

    // Test in WynnItemParser_SPECIAL_PATTERN
    private static final Pattern SPECIAL_PATTERN =
            Pattern.compile("§f.+? (§[2ebc]{1})?(?<special>[A-Za-z ]+).+§7Tier (?<specialTier>[VI]{1,3})");

    // Test in WynnItemParser_TOOLTIP_PAGE_PATTERN
    private static final Pattern TOOLTIP_PAGE_PATTERN = Pattern.compile("(§#ffea80ff)?\uE000");

    private static final Map<CustomColor, Integer> TIER_COLOR_CODES = Map.of(
            CommonColors.BLACK,
            0,
            CustomColor.fromInt(0xe6e647),
            1,
            CustomColor.fromInt(0xe647e6),
            2,
            CustomColor.fromInt(0x47e6e6),
            3);

    // Test in WynnItemParser_PROFESSION_TIER_PATTERN
    private static final Pattern PROFESSION_TIER_PATTERN = Pattern.compile(
            "^§f\uDB00\uDC26.*?(?:(?:§#(?<hexColor>e6e647|e647e6|47e6e6)ff)|(?<blackColor>§0))\uE000{1,3}\uDB00\uDC02.*$");

    private static WynnItemHeaderParseResult parseHeader(List<Component> lore, String itemType) {
        int dps = 0;
        int health = 0;
        GearAttackSpeed attackSpeed = null;
        List<Pair<DamageType, RangedValue>> damages = new ArrayList<>();
        List<Pair<Element, Integer>> defences = new ArrayList<>();
        CappedValue durability = CappedValue.EMPTY;
        CappedValue uses = CappedValue.EMPTY;
        int durationSeconds = 0;
        GearTier tier = null;
        GearRestrictions restrictions = GearRestrictions.NONE;
        boolean parsingDefenses = false;
        boolean parsingDamages = false;
        for (Component line : lore) {
            StyledText normalizedCoded = StyledText.fromComponent(line).getNormalized();
            StyledTextPart lastPart = normalizedCoded.getLastPart();
            if (lastPart != null && lastPart.getPartStyle().getFont().equals(CommonFonts.TOOLTIP_DIVIDER_FONT)) {
                break;
            }

            Matcher tierMatcher = normalizedCoded.getMatcher(TIER_PATTERN);
            if (tierMatcher.matches()) {
                if (tier == null) {
                    String chatColor = tierMatcher.group(1);
                    if (chatColor != null) {
                        ChatFormatting chatFormatting = ChatFormatting.getByCode(chatColor.charAt(0));
                        tier = GearTier.fromChatFormatting(chatFormatting);
                    } else {
                        tier = GearTier.NORMAL;
                    }
                    String tierLine = normalizedCoded.getStringWithoutFormatting();

                    char untradableChar = WynnFont.toGlyph("untradable_foreground", TooltipBannerFont.class)
                            .charAt(0);
                    char questChar = WynnFont.toGlyph("quest_foreground", TooltipBannerFont.class)
                            .charAt(0);
                    char restrictionChar = tierLine.charAt(tierLine.length() - 1);

                    if (restrictionChar == untradableChar) {
                        restrictions = GearRestrictions.UNTRADABLE;
                    } else if (restrictionChar == questChar) {
                        restrictions = GearRestrictions.QUEST_ITEM;
                    } else {
                        restrictions = GearRestrictions.NONE;
                    }
                }
                continue;
            }

            Matcher healthMatcher = normalizedCoded.getMatcher(HEALTH_PATTERN);
            if (healthMatcher.matches()) {
                health = Integer.parseInt(healthMatcher.group(1).replace(",", ""));
                continue;
            }

            Matcher dpsMatcher = normalizedCoded.getMatcher(DPS_PATTERN);
            if (dpsMatcher.matches()) {
                dps = Integer.parseInt(dpsMatcher.group(1).replace(",", ""));
                continue;
            }

            Matcher durabilityMatcher = normalizedCoded.getMatcher(DURABILITY_PATTERN);
            if (durabilityMatcher.matches()) {
                durability = new CappedValue(
                        Integer.parseInt(durabilityMatcher.group(1)), Integer.parseInt(durabilityMatcher.group(2)));
                continue;
            }

            String plainText = normalizedCoded.getStringWithoutFormatting().trim();

            Matcher chargesMatcher = CONSUMABLE_CHARGES_PATTERN.matcher(plainText);
            if (chargesMatcher.matches()) {
                uses = new CappedValue(
                        Integer.parseInt(chargesMatcher.group("current")),
                        Integer.parseInt(chargesMatcher.group("max")));
                continue;
            }

            Matcher durationMatcher = CONSUMABLE_DURATION_PATTERN.matcher(plainText);
            if (durationMatcher.matches()) {
                durationSeconds = parseDisplayedDuration(durationMatcher.group("duration"));
                continue;
            }

            Matcher attackSpeedMatcher = normalizedCoded.getMatcher(ITEM_ATTACK_SPEED_PATTERN);
            if (attackSpeedMatcher.matches()) {
                String speedName = attackSpeedMatcher.group(1);
                attackSpeed = GearAttackSpeed.fromString(speedName);
                parsingDamages = true;
                continue;
            }

            if (parsingDamages) {
                if (normalizedCoded.isEmpty()) continue;

                if (!normalizedCoded.getLastPart().getPartStyle().getFont().equals(CommonFonts.TOOLTIP_DIVIDER_FONT)) {
                    Matcher damageMatcher = normalizedCoded.getMatcher(ITEM_DAMAGE_PATTERN);

                    while (damageMatcher.find()) {
                        String symbol = damageMatcher.group("spriteSymbol");
                        RangedValue range = RangedValue.fromString(damageMatcher.group("range"));
                        damages.add(Pair.of(DamageType.fromTooltipSprite(symbol), range));
                    }
                    continue;
                }
            }

            if (!parsingDefenses && normalizedCoded.equalsString(ELEMENTAL_DEFENSES_LINE)) {
                parsingDefenses = true;
                continue;
            }

            if (parsingDefenses) {
                if (normalizedCoded.isEmpty()) continue;

                if (!normalizedCoded.getLastPart().getPartStyle().getFont().equals(CommonFonts.TOOLTIP_DIVIDER_FONT)) {
                    Matcher defenseMatcher = normalizedCoded.getMatcher(ITEM_DEFENCE_PATTERN);

                    while (defenseMatcher.find()) {
                        Element element = Element.fromTooltipSprite(defenseMatcher.group("spriteSymbol"));
                        int value = Integer.parseInt(defenseMatcher.group("value"));
                        defences.add(Pair.of(element, value));
                    }
                }
            }
        }

        return new WynnItemHeaderParseResult(
                tier,
                itemType,
                dps,
                health,
                attackSpeed,
                damages,
                defences,
                durability,
                uses,
                durationSeconds,
                restrictions);
    }

    public static WynnItemStatsParseResult parseItemStackStatsPage(
            ItemStack itemStack, Map<StatType, StatPossibleValues> possibleValuesMap) {
        List<StatActualValue> identifications = new ArrayList<>();
        List<NamedItemEffect> namedEffects = new ArrayList<>();
        List<ItemEffect> effects = new ArrayList<>();
        int levelReq = 0;
        List<Pair<Skill, Integer>> skillReqs = new ArrayList<>();
        ClassType classReq = null;
        String questReq = null;
        int questLevel = 0;
        int rerolls = 0;
        Optional<ShinyStat> shinyStat = Optional.empty();
        boolean levelReqMet = true;
        boolean classReqMet = true;
        boolean questReqMet = true;
        Map<Skill, Boolean> skillReqsMet = Arrays.stream(Skill.values())
                .collect(
                        Collectors.toMap(skill -> skill, skill -> true, (a, b) -> a, () -> new EnumMap<>(Skill.class)));
        List<String> craftedLore = new ArrayList<>();
        String craftedCreator = "";

        // Parse lore for identifications, powders and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(LoreUtils.getTooltipLines(itemStack));
        lore.removeFirst(); // remove item name
        lore.removeLast(); // remove empty last line

        WynnItemHeaderParseResult header = parseHeader(lore, extractFrameSpriteCode(itemStack));
        if (header.durationSeconds() > 0) {
            namedEffects.add(new NamedItemEffect(ConsumableEffect.DURATION, header.durationSeconds()));
        }

        // The stats page is split into up to 4 segments, most items will use 3
        // The first contains the item name, type, rarity, health/damage
        // attack speed (if applicable) and element defenses/damages
        // The second contains the item requirements, these start out
        // with skill points, all 5 skills are displayed regardless of
        // whether they are needed or not. They are then followed by
        // any quest requirement, the class type and combat level.
        // The third segment is currently only used for shiny information
        // The fourth segment contains rerolls and identifications

        int segment = 1;

        for (Component loreLine : lore) {
            StyledText coded = StyledText.fromComponent(loreLine);
            StyledText normalizedCoded = coded.getNormalized();

            if (segment == 1) {
                StyledTextPart lastPart = normalizedCoded.getLastPart();
                if (lastPart != null && lastPart.getPartStyle().getFont().equals(CommonFonts.TOOLTIP_DIVIDER_FONT)) {
                    segment++;
                }
                continue;
            }

            // Requirements
            if (segment == 2) {
                // Combat level
                Matcher levelMatcher = normalizedCoded.getMatcher(MIN_LEVEL_PATTERN);
                if (levelMatcher.matches()) {
                    levelReq = Integer.parseInt(levelMatcher.group("level"));

                    String mark = levelMatcher.group(1);
                    if (mark.contains("\uE007")) {
                        levelReqMet = false;
                    }

                    continue;
                }

                // Skills
                Matcher skillMatcherUpper = normalizedCoded.getMatcher(SKILL_REQ_PATTERN);
                if (skillMatcherUpper.matches()) {
                    Matcher partMatcher = normalizedCoded.getMatcher(SKILL_REQ_PART_PATTERN);
                    int index = 0;
                    while (partMatcher.find()) {
                        Skill skill = Skill.values()[index];
                        skillReqs.add(Pair.of(skill, Integer.parseInt(partMatcher.group(2))));

                        if (partMatcher.group(1).equals("\uE007")) {
                            skillReqsMet.put(skill, false);
                        }

                        index++;
                    }

                    if (index != Skill.values().length) {
                        WynntilsMod.warn("Unexpected skill point requirement count from " + itemStack.getHoverName()
                                + "(" + (index + 1) + ")");
                    }

                    continue;
                }

                // Class
                Matcher classMatcher = normalizedCoded.getMatcher(CLASS_REQ_PATTERN);
                if (classMatcher.matches()) {
                    String className = classMatcher.group("name");
                    classReq = ClassType.fromName(className);

                    String mark = classMatcher.group(1);
                    if (mark.contains("\uE007")) {
                        classReqMet = false;
                    }

                    continue;
                }

                // Quests
                Matcher questMatcher = normalizedCoded.getMatcher(QUEST_REQ_PATTERN);
                if (questMatcher.matches()) {
                    questReq = questMatcher.group(2);
                    questLevel = Integer.parseInt(questMatcher.group("level"));

                    String mark = questMatcher.group(1);
                    if (mark.contains("\uE007")) {
                        questReqMet = false;
                    }

                    continue;
                }

                StyledTextPart lastPart = normalizedCoded.getLastPart();
                if (lastPart != null && lastPart.getPartStyle().getFont().equals(CommonFonts.TOOLTIP_DIVIDER_FONT)) {
                    segment++;
                    continue;
                }
            }

            // Optional shiny stat
            if (segment == 3) {
                if (normalizedCoded.stripAlignment().isBlank()) continue;
                // Look for shiny stat
                Matcher shinyStatMatcher = normalizedCoded.getMatcher(SHINY_STAT_PATTERN);
                if (shinyStatMatcher.matches() && shinyStat.isEmpty()) {
                    String shinyName = shinyStatMatcher.group(1);
                    int shinyValue = Integer.parseInt(shinyStatMatcher.group(2).replace(",", ""));
                    int shinyRerolls = Math.max(0, parseBannerNumber(shinyStatMatcher.group(3)));
                    shinyStat =
                            Optional.of(new ShinyStat(Models.Shiny.getShinyStat(shinyName), shinyValue, shinyRerolls));
                    segment++;
                    continue;
                }
                segment++;
                // fall through
            }

            if (segment == 4) {
                Matcher rerollMatcher = normalizedCoded.getMatcher(REROLL_PATTERN);
                if (rerollMatcher.matches()) {
                    rerolls = parseBannerNumber(rerollMatcher.group("banner"));
                    continue;
                }

                // Look for identifications
                Matcher statMatcher = normalizedCoded.getMatcher(IDENTIFICATION_STAT_PATTERN);
                if (statMatcher.matches()) {
                    String statDisplayName = statMatcher.group("statName");
                    int value = Integer.parseInt(statMatcher.group("value").replace(",", ""));
                    String unit = statMatcher.group("unit");

                    StatType statType = possibleValuesMap == null
                            ? Models.Stat.fromDisplayName(statDisplayName, unit)
                            : Models.Stat.fromDisplayName(statDisplayName, unit, possibleValuesMap.keySet());
                    if (statType == null) {
                        WynntilsMod.warn(
                                "Item " + itemStack.getHoverName() + " has unknown identified stat " + statDisplayName);
                        continue;
                    }
                    if (statType.calculateAsInverted()) {
                        // Spell Cost stats are shown as negative, but we store them as positive
                        value = -value;
                    }

                    boolean perfectInternalRoll = statMatcher.group("indicatorColor") != null
                            && statMatcher
                                    .group("indicatorColor")
                                    .equals(WynncraftShaderColor.RAINBOW.color.toHexString());
                    String vanillaMeterGroup = statMatcher.group("vanillaMeter");
                    Optional<Integer> vanillaMeter = vanillaMeterGroup == null
                            ? Optional.empty()
                            : Optional.of(vanillaMeterGroup.codePointAt(0));

                    // Load the possible values for this stat
                    StatPossibleValues possibleValues =
                            possibleValuesMap != null ? possibleValuesMap.get(statType) : null;

                    StatActualValue actualValue = Models.Stat.buildActualValue(
                            statType,
                            value,
                            perfectInternalRoll,
                            possibleValues,
                            vanillaMeter.isPresent()
                                    ? TooltipIdentificationMeterFont.cappedValueFromCharacter(vanillaMeter.get())
                                    : CappedValue.EMPTY);
                    identifications.add(actualValue);
                    continue;
                }

                Matcher craftedLoreMatcher = normalizedCoded.getMatcher(LORE_PATTERN);
                if (craftedLoreMatcher.matches()) {
                    craftedLore.add(craftedLoreMatcher.group("lore"));
                    continue;
                }

                Matcher crafterMatcher = normalizedCoded.getMatcher(CRAFTED_BY_PATTERN);
                if (crafterMatcher.matches()) {
                    craftedCreator = crafterMatcher.group("creator");
                }
            }
        }

        return new WynnItemStatsParseResult(
                header.tier(),
                header.itemType(),
                header.dps(),
                header.health(),
                levelReq,
                header.attackSpeed(),
                header.damages(),
                header.defences(),
                new GearRequirements(
                        levelReq, Optional.ofNullable(classReq), skillReqs, Optional.ofNullable(questReq), questLevel),
                identifications,
                namedEffects,
                effects,
                rerolls,
                header.durability(),
                header.uses(),
                header.durationSeconds(),
                String.join(" ", craftedLore),
                craftedCreator,
                shinyStat,
                new GearInstanceRequirements(levelReqMet, classReqMet, skillReqsMet, questReqMet),
                parseMajorIdLines(lore),
                header.restrictions());
    }

    private static String parseSetName(String text) {
        String name = "";
        Matcher matcher = BLACK_BANNER_TEXT_PATTERN.matcher(text);

        while (matcher.find()) {
            String decoded = BannerBoxFont.decodeForeground(matcher.group(1));
            if (!decoded.isEmpty() && !decoded.equals("set")) {
                name = decoded;
            }
        }

        return name;
    }

    private static List<Component> parseMajorIdLines(List<Component> lore) {
        List<Component> lines = new ArrayList<>();
        boolean inMajorId = false;

        for (Component line : lore) {
            StyledText text = StyledText.fromComponent(line).getNormalized();
            boolean header = text.getMatcher(MAJOR_ID_HEADER_PATTERN).matches();
            inMajorId = header
                    || (inMajorId
                            && !text.stripAlignment().isBlank()
                            && text.getMatcher(MAJOR_ID_DESCRIPTION_PATTERN).matches());

            if (inMajorId) {
                lines.add(line);
            }
        }

        return lines;
    }

    public static WynnItemMiscStatsParseResult parseItemStackMiscStatsPage(ItemStack itemStack) {
        List<Component> lore = ComponentUtils.stripDuplicateBlank(LoreUtils.getTooltipLines(itemStack));
        lore.removeFirst(); // remove item name
        lore.removeLast(); // remove empty last line

        String itemType = extractFrameSpriteCode(itemStack);

        PowderSpecialTierInfo specialInfo = null;
        List<Pair<Powder, Integer>> powders = new ArrayList<>();
        int powderSlots = 0;

        // The misc stats page is split into up to 4 segments, most items will use 2
        // The first contains the item name, type, rarity, health/damage
        // attack speed (if applicable) and element defenses/damages
        // The second contains the gear restriction (if applicable)
        // The third contains the set bonuses (if applicable)
        // The fourth segment contains the powder info
        // The powder segment is displayed regardless of if the item supports
        // powders or not

        int powdersToParse = 0;
        boolean parsingPowders = false;
        boolean parsingSetBonuses = false;
        int equippedCount = 0;
        int totalCount = 0;
        boolean hasSet = false;
        String setName = "";
        Map<StatType, Integer> setBonuses = new HashMap<>();
        List<Component> setBonusLines = new ArrayList<>();

        for (Component loreLine : lore) {
            StyledText coded = StyledText.fromComponent(loreLine);
            StyledText normalizedCoded = coded.getNormalized();

            if (normalizedCoded.isEmpty()) {
                if (parsingSetBonuses) {
                    setBonusLines.add(loreLine);
                }
                continue;
            }

            if (parsingPowders) {
                Matcher powderTierMatcher = normalizedCoded.getMatcher(POWDER_PATTERN);
                if (powdersToParse > 0 && powderTierMatcher.find()) {
                    powdersToParse--;
                    Powder powderOne = Powder.fromName(powderTierMatcher.group("powderOne"));
                    int powderOneTier = MathUtils.integerFromRoman(powderTierMatcher.group("powderOneTier"));

                    powders.add(Pair.of(powderOne, powderOneTier));

                    if (powderTierMatcher.group("powderTwo") != null) {
                        Powder powderTwo = Powder.fromName(powderTierMatcher.group("powderTwo"));
                        int powderTwoTier = MathUtils.integerFromRoman(powderTierMatcher.group("powderTwoTier"));
                        powders.add(Pair.of(powderTwo, powderTwoTier));
                        powdersToParse--;
                    }
                    continue;
                }

                Matcher specialMatcher = normalizedCoded.getMatcher(SPECIAL_PATTERN);
                if (specialMatcher.matches()) {
                    specialInfo = Models.Element.getPowderSpecialTierInfo(
                            specialMatcher.group("special"),
                            MathUtils.integerFromRoman(specialMatcher.group("specialTier")));
                    continue;
                }

                continue;
            }

            Matcher setMatcher = normalizedCoded.getMatcher(SET_PATTERN);
            if (setMatcher.matches()) {
                setName = parseSetName(normalizedCoded.getString());
                parsingSetBonuses = true;
                hasSet = true;
                equippedCount = Integer.parseInt(setMatcher.group("equipped"));
                totalCount = Integer.parseInt(setMatcher.group("total"));
                continue;
            }

            Matcher powderMatcher = normalizedCoded.getMatcher(POWDER_TITLE_PATTERN);
            if (powderMatcher.matches()) {
                parsingSetBonuses = false;
                parsingPowders = true;
                powderSlots = Integer.parseInt(powderMatcher.group("slots"));
                powdersToParse = Integer.parseInt(powderMatcher.group("applied"));
                continue;
            }

            if (parsingSetBonuses) {
                setBonusLines.add(loreLine);
                Matcher setBonusMatcher = normalizedCoded.getMatcher(IDENTIFICATION_STAT_PATTERN);
                if (setBonusMatcher.matches()) {
                    StatType statType = Models.Stat.fromDisplayName(
                            setBonusMatcher.group("statName"), setBonusMatcher.group("unit"));
                    if (statType != null) {
                        int value =
                                Integer.parseInt(setBonusMatcher.group("value").replace(",", ""));
                        setBonuses.put(statType, value);
                    }
                }
            }
        }

        WynnItemHeaderParseResult header = parseHeader(lore, itemType);
        return new WynnItemMiscStatsParseResult(
                header.tier(),
                header.itemType(),
                header.dps(),
                header.health(),
                header.attackSpeed(),
                header.damages(),
                header.defences(),
                powders,
                powderSlots,
                header.durability(),
                Optional.ofNullable(specialInfo),
                hasSet
                        ? Optional.of(new SetInstance(
                                equippedCount, totalCount, setBonuses, setName, parseMajorIdLines(setBonusLines)))
                        : Optional.empty(),
                header.restrictions());
    }

    public static WynnItemLoreParseResult parseItemStackLorePage(ItemStack itemStack) {
        List<Component> lines = ComponentUtils.stripDuplicateBlank(LoreUtils.getTooltipLines(itemStack));
        lines.removeFirst(); // remove item name
        lines.removeLast(); // remove empty last line

        String itemType = extractFrameSpriteCode(itemStack);

        List<StyledText> lore = new ArrayList<>();
        String creator = "";
        boolean pastHeader = false;
        boolean collectingText = false;
        boolean expectBannerBlank = false;

        // The lore page is split into up to 2 segments
        // The first contains the item name, type, rarity, health/damage
        // attack speed (if applicable) and element defenses/damages
        // The second contains the item lore and the name of the player
        // who created the item if it is a crafted item

        for (Component line : lines) {
            StyledText normalizedCoded = StyledText.fromComponent(line).getNormalized();
            StyledTextPart lastPart = normalizedCoded.getLastPart();
            if (!pastHeader) {
                if (lastPart != null && lastPart.getPartStyle().getFont().equals(CommonFonts.TOOLTIP_DIVIDER_FONT)) {
                    pastHeader = true;
                }
                continue;
            }

            Matcher bannerMatcher = normalizedCoded.getMatcher(COMMUNITY_EVENT_WINNER_PATTERN);
            if (bannerMatcher.matches()) {
                int loreLength =
                        StyledText.fromString(bannerMatcher.group("lore")).length();
                lore.add(normalizedCoded.substring(normalizedCoded.length() - loreLength));
                expectBannerBlank = true;
                collectingText = false;
                continue;
            }

            if (normalizedCoded.stripAlignment().isBlank()) {
                if (expectBannerBlank || collectingText) {
                    lore.add(StyledText.EMPTY);
                }
                expectBannerBlank = false;
                collectingText = false;
                continue;
            }

            Matcher crafterMatcher = normalizedCoded.getMatcher(CRAFTED_BY_PATTERN);
            if (crafterMatcher.matches()) {
                creator = StyledText.fromString(crafterMatcher.group("creator")).getStringWithoutFormatting();
                if (!lore.isEmpty() && lore.getLast().isEmpty()) {
                    lore.removeLast();
                }
                break;
            }

            Matcher loreMatcher = normalizedCoded.getMatcher(LORE_PAGE_PATTERN);
            if (loreMatcher.matches()) {
                int loreLength =
                        StyledText.fromString(loreMatcher.group("lore")).length();
                StyledText loreLine = normalizedCoded.substring(normalizedCoded.length() - loreLength);
                if (collectingText) {
                    lore.set(lore.size() - 1, lore.getLast().append(" ").append(loreLine));
                } else {
                    lore.add(loreLine);
                }
                collectingText = true;
            } else {
                break;
            }
        }

        WynnItemHeaderParseResult header = parseHeader(lines, itemType);
        return new WynnItemLoreParseResult(
                header.tier(),
                header.itemType(),
                header.dps(),
                header.health(),
                header.attackSpeed(),
                header.damages(),
                header.defences(),
                header.durability(),
                header.uses(),
                header.durationSeconds(),
                header.restrictions(),
                StyledText.join("\n", lore),
                creator);
    }

    private static int parseDisplayedDuration(String durationText) {
        Matcher matcher = DISPLAY_DURATION_PATTERN.matcher(durationText);
        if (!matcher.matches()) {
            return 0;
        }

        int hours = matcher.group("hours") != null ? Integer.parseInt(matcher.group("hours")) : 0;
        int minutes = matcher.group("minutes") != null ? Integer.parseInt(matcher.group("minutes")) : 0;
        int seconds = Integer.parseInt(matcher.group("seconds"));
        return hours * 3600 + minutes * 60 + seconds;
    }

    public static int parseProfessionTier(ItemStack itemStack) {
        Matcher tierMatcher = LoreUtils.matchLoreLine(itemStack, 1, PROFESSION_TIER_PATTERN);
        if (!tierMatcher.matches()) {
            return -1;
        }

        if (tierMatcher.group("blackColor") != null) {
            return 0;
        }

        String tierColor = tierMatcher.group("hexColor");
        return tierColor == null ? -1 : TIER_COLOR_CODES.getOrDefault(CustomColor.fromHexString(tierColor), 0);
    }

    public static int parseTooltipPage(ItemStack itemStack) {
        // The same glyph is also used by headers and dividers. Only the page font
        // identifies the paginator; matching any occurrence can reset saved stats.
        for (StyledText line : LoreUtils.getLore(itemStack).reversed()) {
            for (StyledTextPart part : line) {
                if (part.getPartStyle().getFont().equals(CommonFonts.TOOLTIP_PAGE_FONT)) {
                    return parseTooltipPage(line);
                }
            }
        }
        return 0;
    }

    private static int parseTooltipPage(StyledText styledText) {
        Matcher matcher = styledText.getMatcher(TOOLTIP_PAGE_PATTERN);
        int index = 0;

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                return index;
            }

            index++;
        }

        return 0;
    }

    private static int parseBannerNumber(String text) {
        Matcher matcher = REROLL_EXTRACT_PATTERN.matcher(text);
        String rawNumberSegment = null;

        while (matcher.find()) {
            rawNumberSegment = matcher.group(1);
        }

        if (rawNumberSegment == null) {
            return -1;
        }

        int rerolls = 0;

        for (int i = 0; i < rawNumberSegment.length(); i++) {
            char c = rawNumberSegment.charAt(i);

            switch (c) {
                case '\uE050' -> rerolls = rerolls * 10;
                case '\uE051' -> rerolls = rerolls * 10 + 1;
                case '\uE052' -> rerolls = rerolls * 10 + 2;
                case '\uE053' -> rerolls = rerolls * 10 + 3;
                case '\uE054' -> rerolls = rerolls * 10 + 4;
                case '\uE055' -> rerolls = rerolls * 10 + 5;
                case '\uE056' -> rerolls = rerolls * 10 + 6;
                case '\uE057' -> rerolls = rerolls * 10 + 7;
                case '\uE058' -> rerolls = rerolls * 10 + 8;
                case '\uE059' -> rerolls = rerolls * 10 + 9;
                default -> {
                    // Ignore
                }
            }
        }

        return rerolls;
    }

    public static String extractFrameSpriteCode(ItemStack itemStack) {
        List<StyledText> lines = LoreUtils.getLore(itemStack);

        for (StyledText line : lines) {
            for (StyledTextPart part : line) {
                if (part.getPartStyle().getFont().equals(CommonFonts.TOOLTIP_EMBLEM_SPRITE_FONT)) {
                    return part.getString(null, StyleType.NONE);
                }
            }
        }

        return "";
    }
}
