/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.models.wynnitem.type.NamedItemEffect;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class WynnItemParser {
    private static final FontDescription DIVIDER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/divider"));

    private static final Pattern HEALTH_PATTERN =
            Pattern.compile("^§f\uDB00\uDC02§#(?:[a-f0-9]{8})([+-][\\d,]+)§f Health$");

    private static final Pattern DURABILITY_PATTERN =
            Pattern.compile("§f\uDB00\uDC02§8\uE023\uDAFF\uDFF7§#aed4d4ff.§7 Durability (\\d+)\\/(\\d+)");

    // Test in WynnItemParser_ITEM_ATTACK_SPEED_PATTERN
    private static final Pattern ITEM_ATTACK_SPEED_PATTERN =
            Pattern.compile("^§f\uDB00\uDC02\uE007§7 ([\\w ]+) §8\\((\\d+.\\d+) hits\\/s\\)$");

    // Test in WynnItemParser_ITEM_DAMAGE_PATTERN
    private static final Pattern ITEM_DAMAGE_PATTERN =
            Pattern.compile("§f[^§]*?(?<spriteSymbol>[\uE000-\uE005])[^§]*?§7(?<range>\\d+-\\d+)");

    private static final String ELEMENTAL_DEFENSES_LINE = "§f\uDB00\uDC02§7Elemental Defences";

    // Test in WynnItemParser_ITEM_DEFENCE_PATTERN
    private static final Pattern ITEM_DEFENCE_PATTERN =
            Pattern.compile("§f[^§]*?(?<spriteSymbol>[\uE000-\uE004])[^§]*?§7(?<value>[+-]?\\d+)");

    // Test in WynnItemParser_IDENTIFICATION_STAT_PATTERN
    public static final Pattern IDENTIFICATION_STAT_PATTERN = Pattern.compile(
            "§f(?<statName>[\\w\\.\\- ]+).+?§#(acfac6ff|faacacff)(?<value>[-+][\\d,]+)(?<unit>%| tier|\\/[35]s)?(?:§f §8\uE023\uDAFF\uDFF7§(?<indicatorColor>#[a-zA-Z0-9]{8})(.)?)?");

    // Test in WynnItemParser_TIER_PATTERN
    private static final Pattern TIER_PATTERN = Pattern.compile("§f\uDB00\uDC23§([5bcdef]).+");

    private static final Pattern REROLL_EXTRACT_PATTERN = Pattern.compile("\uE060(.*?)\uE062");

    // Test in WynnItemParser_MIN_LEVEL_PATTERN
    private static final Pattern MIN_LEVEL_PATTERN =
            Pattern.compile("^§f(\uE006|\uE007)\uDAFF\uDFFF Combat Level.+?§7(?<level>\\d+)$");

    // Test in WynnItemParser_CLASS_REQ_PATTERN
    private static final Pattern CLASS_REQ_PATTERN =
            Pattern.compile("^§f(\uE006|\uE007)\uDAFF\uDFFF Class Type.+?§7(?<name>.+)\\/(?<skinned>.+)$");

    private static final String SKILL_REQ_PART = ".+?(\uE005|\uE006|\uE007).+?§(?:8|#acfac6ff|#faacacff)(\\d+).+?";
    private static final Pattern SKILL_REQ_PART_PATTERN = Pattern.compile(SKILL_REQ_PART);

    // Test in WynnItemParser_SKILL_REQ_PATTERN
    private static final Pattern SKILL_REQ_PATTERN = Pattern.compile("(?:" + SKILL_REQ_PART + "){5}");

    // Test in WynnItemParser_QUEST_REQ_PATTERN
    private static final Pattern QUEST_REQ_PATTERN =
            Pattern.compile("^§f(\uE006|\uE007)\uDAFF\uDFFF Quest.+?§7([\\w\\. ]+) §8\\(Lv\\. (?<level>\\d+)\\)$");

    public static final Pattern SET_PATTERN = Pattern.compile("§a(.+) Set §7\\((\\d)/\\d\\)");

    public static final Pattern SET_BONUS_PATTERN = Pattern.compile("^§aSet Bonus:(?:§r)?$");

    // Checks for items eg. "- Morph-Emerald" to determine if item is equipped from color
    public static final Pattern SET_ITEM_PATTERN = Pattern.compile("^§[a7]- §([28])(.+)");

    // Test in WynnItemParser_SHINY_STAT_PATTERN
    private static final Pattern SHINY_STAT_PATTERN = Pattern.compile(
            "^§f\uE04F\uDAFF\uDFFF§#(?:[a-f0-9]{8}) ([a-zA-Z ]+).+?§f([\\d,]+)§#(?:[a-f0-9]{8})(\uDB00\uDC00|.+)$");

    // Test in WynnItemParser_TOOLTIP_PAGE_PATTERN
    private static final Pattern TOOLTIP_PAGE_PATTERN = Pattern.compile("(§#ffea80ff)?\uE000");

    private static final Map<CustomColor, Integer> TIER_COLOR_CODES = Map.of(
            CommonColors.BLACK,
            0,
            CustomColor.fromInt(0xebeb47),
            1,
            CustomColor.fromInt(0xeb47eb),
            2,
            CustomColor.fromInt(0x47ebeb),
            3);

    private static final Pattern PROFESSION_TIER_PATTERN =
            Pattern.compile(".+?(?:§(0|#([a-f0-9]{8})))(?:\uE000){1,3}.+?");

    private static final FontDescription SPRITE_FRAME_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/sprite"));

    public static WynnItemParseResult parseItemStack(
            ItemStack itemStack, Map<StatType, StatPossibleValues> possibleValuesMap) {
        List<StatActualValue> identifications = new ArrayList<>();
        List<NamedItemEffect> namedEffects = new ArrayList<>();
        List<ItemEffect> effects = new ArrayList<>();
        List<Powder> powders = new ArrayList<>();
        int powderSlots = 0;
        int health = 0;
        GearAttackSpeed attackSpeed = null;
        List<Pair<DamageType, RangedValue>> damages = new ArrayList<>();
        List<Pair<Element, Integer>> defences = new ArrayList<>();
        int levelReq = 0;
        List<Pair<Skill, Integer>> skillReqs = new ArrayList<>();
        ClassType classReq = null;
        String questReq = null;
        int rerolls = 0;
        CappedValue durability = CappedValue.EMPTY;
        GearTier tier = null;
        String itemType = extractFrameSpriteCode(itemStack);
        Optional<ShinyStat> shinyStat = Optional.empty();
        boolean allRequirementsMet = true;
        SetInfo setInfo = null;
        Map<String, Boolean> activeItems = new HashMap<>();
        int setWynnCount = 0;
        Map<StatType, Integer> wynnBonuses = new HashMap<>();

        // Parse lore for identifications, powders and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(LoreUtils.getTooltipLines(itemStack));
        lore.removeFirst(); // remove item name
        lore.removeLast(); // remove empty last line

        int currentPage = parseTooltipPage(lore.getLast());

        // Tooltips are split into up to 4 segments, most items will use 3
        // The first contains the item name, type, rarity, health/damage
        // attack speed (if applicable) and element defenses/damages
        // The second contains the item requirements, these start out
        // with skill points, all 5 skills are displayed regardless of
        // whether they are needed or not. They are then followed by
        // any quest requirement, the class type and combat level.
        // The third segment is currently only used for shiny information
        // The fourth segment contains rerolls and identifications
        // Powder slots and set info are on the 2nd page and cannot be
        // parsed with the rest of the item info

        int segment = 1;

        // Defenses appear after the header line
        boolean parsingDefenses = false;
        // Damages appear after the attack speed line
        boolean parsingDamages = false;

        for (Component loreLine : lore) {
            StyledText coded = StyledText.fromComponent(loreLine);
            StyledText normalizedCoded = coded.getNormalized();

            if (segment == 1) {
                Matcher tierMatcher = normalizedCoded.getMatcher(TIER_PATTERN);
                if (tierMatcher.matches()) {
                    ChatFormatting chatFormatting =
                            ChatFormatting.getByCode(tierMatcher.group(1).charAt(0));
                    tier = GearTier.fromChatFormatting(chatFormatting);
                    continue;
                }

                Matcher healthMatcher = normalizedCoded.getMatcher(HEALTH_PATTERN);
                if (healthMatcher.matches()) {
                    health = Integer.parseInt(healthMatcher.group(1).replace(",", ""));
                    continue;
                }

                Matcher durabilityMatcher = normalizedCoded.getMatcher(DURABILITY_PATTERN);
                if (durabilityMatcher.matches()) {
                    durability = new CappedValue(
                            Integer.parseInt(durabilityMatcher.group(1)), Integer.parseInt(durabilityMatcher.group(2)));
                    continue;
                }

                Matcher attackSpeedMatcher = coded.getMatcher(ITEM_ATTACK_SPEED_PATTERN);
                if (attackSpeedMatcher.matches()) {
                    String speedName = attackSpeedMatcher.group(1);
                    attackSpeed = GearAttackSpeed.fromString(speedName);
                    parsingDamages = true;
                    continue;
                }

                if (parsingDamages) {
                    if (!coded.getLastPart().getPartStyle().getFont().equals(DIVIDER_FONT)) {
                        Matcher damageMatcher = coded.getMatcher(ITEM_DAMAGE_PATTERN);

                        while (damageMatcher.find()) {
                            String symbol = damageMatcher.group("spriteSymbol");
                            RangedValue range = RangedValue.fromString(damageMatcher.group("range"));
                            damages.add(Pair.of(DamageType.fromTooltipSprite(symbol), range));
                        }
                        continue;
                    }
                }

                if (!parsingDefenses && coded.equalsString(ELEMENTAL_DEFENSES_LINE)) {
                    parsingDefenses = true;
                    continue;
                }

                if (parsingDefenses) {
                    if (!coded.getLastPart().getPartStyle().getFont().equals(DIVIDER_FONT)) {
                        Matcher defenseMatcher = coded.getMatcher(ITEM_DEFENCE_PATTERN);

                        while (defenseMatcher.find()) {
                            Element element = Element.fromTooltipSprite(defenseMatcher.group("spriteSymbol"));
                            int value = Integer.parseInt(defenseMatcher.group("value"));
                            defences.add(Pair.of(element, value));
                        }
                        continue;
                    }
                }

                StyledTextPart lastPart = coded.getLastPart();
                if (lastPart != null && lastPart.getPartStyle().getFont().equals(DIVIDER_FONT)) {
                    segment++;
                    continue;
                }
            }

            // Requirements
            if (segment == 2 && currentPage == 0) {
                // Combat level
                Matcher levelMatcher = normalizedCoded.getMatcher(MIN_LEVEL_PATTERN);
                if (levelMatcher.matches()) {
                    levelReq = Integer.parseInt(levelMatcher.group("level"));

                    String mark = levelMatcher.group(1);
                    if (mark.contains("\uE007")) {
                        allRequirementsMet = false;
                    }

                    continue;
                }

                // Skills
                Matcher skillMatcherUpper = normalizedCoded.getMatcher(SKILL_REQ_PATTERN);
                if (skillMatcherUpper.matches()) {
                    Matcher partMatcher = normalizedCoded.getMatcher(SKILL_REQ_PART_PATTERN);
                    int index = 0;
                    while (partMatcher.find()) {
                        if (partMatcher.group(1).equals("\uE007")) {
                            allRequirementsMet = false;
                        }

                        Skill skill = Skill.values()[index];
                        skillReqs.add(Pair.of(skill, Integer.parseInt(partMatcher.group(2))));

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
                        allRequirementsMet = false;
                    }

                    continue;
                }

                // Quests
                Matcher questMatcher = normalizedCoded.getMatcher(QUEST_REQ_PATTERN);
                if (questMatcher.matches()) {
                    questReq = questMatcher.group(2);

                    String mark = questMatcher.group(1);
                    if (mark.contains("\uE006")) {
                        allRequirementsMet = false;
                    }

                    continue;
                }

                StyledTextPart lastPart = coded.getLastPart();
                if (lastPart != null && lastPart.getPartStyle().getFont().equals(DIVIDER_FONT)) {
                    segment++;
                    continue;
                }
            }

            // Optional shiny stat
            if (segment == 3 && currentPage == 0) {
                // Look for shiny stat
                Matcher shinyStatMatcher = normalizedCoded.getMatcher(SHINY_STAT_PATTERN);
                if (shinyStatMatcher.matches() && shinyStat.isEmpty()) {
                    String shinyName = shinyStatMatcher.group(1);
                    int shinyValue = Integer.parseInt(shinyStatMatcher.group(2).replace(",", ""));
                    int shinyRerolls = parseRerolls(shinyStatMatcher.group(3));
                    shinyStat =
                            Optional.of(new ShinyStat(Models.Shiny.getShinyStat(shinyName), shinyValue, shinyRerolls));
                    segment++;
                    continue;
                }
                segment++;
                // fall through
            }

            if (segment == 4 && currentPage == 0) {
                // Look for identifications
                Matcher statMatcher = normalizedCoded.getMatcher(IDENTIFICATION_STAT_PATTERN);
                if (statMatcher.matches()) {
                    String statDisplayName = statMatcher.group("statName");
                    int value = Integer.parseInt(statMatcher.group("value").replace(",", ""));
                    String unit = statMatcher.group("unit");

                    StatType statType = Models.Stat.fromDisplayName(statDisplayName, unit);
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
                            && statMatcher.group("indicatorColor").equals(CommonColors.RAINBOW.toHexString());

                    StatActualValue actualValue = Models.Stat.buildActualValue(statType, value, perfectInternalRoll);
                    identifications.add(actualValue);
                } else {
                    rerolls = parseRerolls(coded.getString(StyleType.DEFAULT));
                }
            }
        }

        return new WynnItemParseResult(
                tier,
                itemType,
                health,
                levelReq,
                attackSpeed,
                damages,
                defences,
                new GearRequirements(levelReq, Optional.ofNullable(classReq), skillReqs, Optional.ofNullable(questReq)),
                identifications,
                namedEffects,
                effects,
                powders,
                powderSlots,
                rerolls,
                durability,
                shinyStat,
                allRequirementsMet,
                Optional.of(new SetInstance(setInfo, activeItems, setWynnCount, wynnBonuses)),
                currentPage);
    }

    public static int parseProfessionTier(ItemStack itemStack) {
        Matcher tierMatcher = LoreUtils.matchLoreLine(itemStack, 1, PROFESSION_TIER_PATTERN);
        String tierColor = "";

        if (tierMatcher.matches()) {
            tierColor = tierMatcher.group(1);
        }

        if (tierColor.isEmpty()) return -1;

        return TIER_COLOR_CODES.getOrDefault(CustomColor.fromHexString(tierColor), 0);
    }

    private static int parseTooltipPage(Component line) {
        StyledText styledText = StyledText.fromComponent(line);
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

    private static int parseRerolls(String rerollsString) {
        if (!rerollsString.endsWith("\uF005")) return 0;

        Matcher matcher = REROLL_EXTRACT_PATTERN.matcher(rerollsString);

        if (!matcher.find()) {
            WynntilsMod.warn("Could not find reroll segment");
            return -1;
        }

        String rawNumberSegment = matcher.group(1);

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
                if (part.getPartStyle().getFont().equals(SPRITE_FRAME_FONT)) {
                    return part.getString(null, StyleType.NONE);
                }
            }
        }

        return "";
    }
}
