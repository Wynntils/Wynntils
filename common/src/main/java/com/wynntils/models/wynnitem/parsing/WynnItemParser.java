/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class WynnItemParser {
    public static final Pattern HEALTH_PATTERN = Pattern.compile("^§4❤ Health: ([+-]\\d+)$");

    // Test suite: https://regexr.com/776qt
    public static final Pattern IDENTIFICATION_STAT_PATTERN = Pattern.compile(
            "^§[ac]([-+]\\d+)(?:§[24] to §[ac](-?\\d+))?(%| tier|/[35]s)?(?:§8/(\\d+)(?:%| tier|/[35]s)?)?(?:§2(\\*{1,3}))? ?§7 ?(.*)$");

    // Test suite: https://regexr.com/782rk
    private static final Pattern TIER_AND_REROLL_PATTERN = Pattern.compile(
            "^(§fNormal|§eUnique|§dRare|§bLegendary|§cFabled|§5Mythic|§aSet|§3Crafted) ([A-Za-z\\d _]+)(?:§8)?(?: \\[(\\d+)(?:\\/(\\d+) Durability)?\\])?$");

    // Test suite: https://regexr.com/778gk
    private static final Pattern POWDER_PATTERN =
            Pattern.compile("^§7\\[(\\d+)/(\\d+)\\] Powder Slots(?: \\[§(.*)§7\\])?$");

    // Test suite: https://regexr.com/79atu
    private static final Pattern EFFECT_LINE_PATTERN = Pattern.compile("^§(.)- §7(.*): §f([+-]?\\d+)(?:§.§.)? ?(.*)$");

    // Test suite: https://regexr.com/798o0
    private static final Pattern MIN_LEVEL_PATTERN = Pattern.compile("^§..§7 Combat Lv. Min: (\\d+)$");

    private static final Pattern EFFECT_HEADER_PATTERN = Pattern.compile("^§(.)Effect:$");

    private static final Pattern POWDER_MARKERS = Pattern.compile("[^✹✦❋❉✤]");

    public static final Pattern SET_BONUS_PATTEN = Pattern.compile("^§aSet Bonus:$");

    // Test suite: https://regexr.com/7i5h5
    public static final Pattern SHINY_STAT_PATTERN = Pattern.compile("^§f⬡ §7([a-zA-Z ]+): §f(\\d+)$");

    public static WynnItemParseResult parseItemStack(
            ItemStack itemStack, Map<StatType, StatPossibleValues> possibleValuesMap) {
        List<StatActualValue> identifications = new ArrayList<>();
        List<ItemEffect> effects = new ArrayList<>();
        List<Powder> powders = new ArrayList<>();
        int health = 0;
        int level = 0;
        int tierCount = 0;
        int durabilityMax = 0;
        GearTier tier = null;
        String itemType = "";
        boolean setBonusStats = false;
        boolean parsingEffects = false;
        Optional<ShinyStat> shinyStat = Optional.empty();
        String effectsColorCode = "";

        // Parse lore for identifications, powders and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(LoreUtils.getTooltipLines(itemStack));
        lore.remove(0); // remove item name

        for (Component loreLine : lore) {
            StyledText coded = StyledText.fromComponent(loreLine);
            StyledText normalizedCoded = coded.getNormalized();

            // Look for powder
            Matcher powderMatcher = normalizedCoded.getMatcher(POWDER_PATTERN);
            if (powderMatcher.matches()) {
                int usedSlots = Integer.parseInt(powderMatcher.group(1));
                String codedPowders = powderMatcher.group(3);
                if (codedPowders == null) continue;

                String powderString = POWDER_MARKERS.matcher(codedPowders).replaceAll("");
                if (powderString.length() != usedSlots) {
                    WynntilsMod.warn("Mismatch between powder slot count " + usedSlots + " and actual powder symbols: "
                            + codedPowders + " for " + itemStack.getHoverName().getString());
                    // Fall through and use codedPowders nevertheless
                }

                codedPowders.chars().forEach(ch -> {
                    Powder powder = Powder.getFromSymbol(Character.toString(ch));
                    if (powder != null) {
                        powders.add(powder);
                    }
                });

                continue;
            }

            // Look for tier and rerolls
            Matcher tierMatcher = normalizedCoded.getMatcher(TIER_AND_REROLL_PATTERN);
            if (tierMatcher.matches()) {
                String tierString = tierMatcher.group(1);
                tier = GearTier.fromStyledText(StyledText.fromString(tierString));
                itemType = tierMatcher.group(2);

                // This is either the rerolls (for re-identified gear), or the
                // current durability (for crafted gear)
                String tierCountString = tierMatcher.group(3);
                tierCount = tierCountString != null ? Integer.parseInt(tierCountString) : 0;

                // If we have a crafted gear, we also have a durability max
                String durabilityMaxString = tierMatcher.group(4);
                durabilityMax = durabilityMaxString != null ? Integer.parseInt(durabilityMaxString) : 0;
                continue;
            }

            Matcher healthMatcher = normalizedCoded.getMatcher(HEALTH_PATTERN);
            if (healthMatcher.matches()) {
                health = Integer.parseInt(healthMatcher.group(1));
                continue;
            }

            // Look for level requirements
            Matcher levelMatcher = normalizedCoded.getMatcher(MIN_LEVEL_PATTERN);
            if (levelMatcher.matches()) {
                level = Integer.parseInt(levelMatcher.group(1));
                continue;
            }

            Matcher setBonusMatcher = normalizedCoded.getMatcher(SET_BONUS_PATTEN);
            if (setBonusMatcher.matches()) {
                // Any stat lines that follow from now on belongs to the Set Bonus
                // Maybe these could be collected separately, but for now, ignore them
                setBonusStats = true;
            }

            // Look for effects (only on consumables)
            Matcher effectHeaderMatcher = normalizedCoded.getMatcher(EFFECT_HEADER_PATTERN);
            if (effectHeaderMatcher.matches()) {
                effectsColorCode = effectHeaderMatcher.group(1);
                parsingEffects = true;
                continue;
            }
            if (parsingEffects) {
                Matcher effectMatcher = normalizedCoded.getMatcher(EFFECT_LINE_PATTERN);
                if (effectMatcher.matches()) {
                    String colorCode = effectMatcher.group(1);
                    String type = effectMatcher.group(2);
                    int value = Integer.parseInt(effectMatcher.group(3));
                    String suffix = effectMatcher.group(4);

                    // A sanity check; otherwise fall through
                    if (colorCode.equals(effectsColorCode)) {
                        // If type is "Heal", "Mana" or "Duration", keep it, otherwise
                        // replace it with the actual effect type
                        if (type.equals("Effect")) {
                            type = suffix;
                        }
                        effects.add(new ItemEffect(type, value));
                        continue;
                    }
                }

                parsingEffects = false;
                // fall through
            }

            // Look for identifications
            Matcher statMatcher = normalizedCoded.getMatcher(IDENTIFICATION_STAT_PATTERN);
            if (statMatcher.matches() && !setBonusStats) {
                int value = Integer.parseInt(statMatcher.group(1));
                // group 2 is only present for unidentified gears, as the to-part of the range
                String unit = statMatcher.group(3);
                // group 4 is only present for crafted gear, as the top value for that stat
                String starString = statMatcher.group(5);
                String statDisplayName = statMatcher.group(6);

                StatType statType = Models.Stat.fromDisplayName(statDisplayName, unit);
                if (statType == null) {
                    // Skill bonuses looks like stats when parsing, ignore them
                    if (Skill.isSkill(statDisplayName)) continue;

                    WynntilsMod.warn(
                            "Item " + itemStack.getHoverName() + " has unknown identified stat " + statDisplayName);
                    continue;
                }
                if (statType.showAsInverted()) {
                    // Spell Cost stats are shown as negative, but we store them as positive
                    value = -value;
                }

                int stars = starString == null ? 0 : starString.length();

                StatPossibleValues possibleValues = possibleValuesMap != null ? possibleValuesMap.get(statType) : null;
                StatActualValue actualValue = Models.Stat.buildActualValue(statType, value, stars, possibleValues);
                identifications.add(actualValue);
            }

            // Look for shiny stat
            Matcher shinyStatMatcher = normalizedCoded.getMatcher(SHINY_STAT_PATTERN);
            if (shinyStatMatcher.matches() && shinyStat.isEmpty()) {
                String shinyName = shinyStatMatcher.group(1);
                int shinyValue = Integer.parseInt(shinyStatMatcher.group(2));
                shinyStat = Optional.of(new ShinyStat(shinyName, shinyValue));
            }
        }

        return new WynnItemParseResult(
                tier,
                itemType,
                health,
                level,
                identifications,
                effects,
                powders,
                tierCount,
                tierCount,
                durabilityMax,
                shinyStat);
    }

    public static WynnItemParseResult parseInternalRolls(GearInfo gearInfo, JsonObject itemData) {
        List<StatActualValue> identifications = new ArrayList<>();

        if (itemData.has("identifications")) {
            JsonArray ids = itemData.getAsJsonArray("identifications");
            for (int i = 0; i < ids.size(); i++) {
                JsonObject idInfo = ids.get(i).getAsJsonObject();
                String id = idInfo.get("type").getAsString();
                int internalRoll = idInfo.get("percent").getAsInt();

                // Lore line is: {type: "<loretype>", percent: <internal roll>}
                // <internal roll> is an integer between 30 and 130

                // First convert loretype (e.g. DAMAGEBONUS) to our StatTypes
                StatType statType = Models.Stat.fromInternalRollId(id);
                if (statType == null) {
                    WynntilsMod.warn("Remote player's " + gearInfo.name() + " contains unknown stat type " + id);
                    continue;
                }

                // Then convert the internal roll
                StatActualValue actualValue = getStatActualValue(gearInfo, statType, internalRoll);
                if (actualValue == null) continue;

                identifications.add(actualValue);
            }
        }

        List<Powder> powders = new ArrayList<>();

        if (itemData.has("powders")) {
            JsonArray powderData = itemData.getAsJsonArray("powders");
            for (int i = 0; i < powderData.size(); i++) {
                String type = powderData.get(i).getAsJsonObject().get("type").getAsString();
                Powder powder = Powder.valueOf(type.toUpperCase(Locale.ROOT));

                powders.add(powder);
            }
        }

        int rerolls = itemData.has("identification_rolls")
                ? itemData.get("identification_rolls").getAsInt()
                : 0;

        // Shiny stats are not available from internal roll lore (on other players)
        return new WynnItemParseResult(
                gearInfo.tier(), "", 0, 0, identifications, List.of(), powders, rerolls, 0, 0, Optional.empty());
    }

    private static StatActualValue getStatActualValue(GearInfo gearInfo, StatType statType, int internalRoll) {
        StatPossibleValues possibleValue = gearInfo.getPossibleValues(statType);
        if (possibleValue == null) {
            if (!(statType instanceof SkillStatType)) {
                // We know Wynncraft send skill stats as 100%; don't complain about that
                WynntilsMod.warn("Remote player's " + gearInfo.name() + " claims to have " + statType);
            }
            return null;
        }
        int value = Math.round(possibleValue.baseValue() * (internalRoll / 100f));
        if (value == 0) {
            // If we get to 0, use 1 or -1 instead
            value = (int) Math.signum(possibleValue.baseValue());
        }

        // Negative values can never show stars
        int stars = (value > 0) ? StatCalculator.calculateStarsFromInternalRoll(internalRoll) : 0;

        // In this case, we actually know the exact internal roll
        return new StatActualValue(statType, value, stars, RangedValue.of(internalRoll, internalRoll));
    }
}
