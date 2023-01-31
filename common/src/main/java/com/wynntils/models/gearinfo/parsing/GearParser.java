/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.parsing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.concepts.Powder;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gearinfo.GearCalculator;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.models.gearinfo.type.GearType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class GearParser {
    // Test suite: https://regexr.com/776qt
    public static final Pattern IDENTIFICATION_STAT_PATTERN = Pattern.compile(
            "^§[ac]([-+]\\d+)(?:§r§[24] to §r§[ac](-?\\d+))?(%| tier|/[35]s)?(?:§r§8/(\\d+)(?:%| tier|/[35]s)?)?(?:§r§2(\\*{1,3}))? ?§r§7 ?(.*)$");

    // Test suite: https://regexr.com/778gh
    private static final Pattern TIER_AND_REROLL_PATTERN = Pattern.compile(
            "^(§fNormal|§eUnique|§dRare|§bLegendary|§cFabled|§5Mythic|§aSet|§3Crafted) ([A-Za-z ]+)(?:§r§8)?(?: \\[(\\d+)(?:/(\\d+) Durability)?\\])?$");

    // Test suite: https://regexr.com/778gk
    private static final Pattern POWDER_PATTERN =
            Pattern.compile("^§7\\[(\\d+)/(\\d+)\\] Powder Slots(?: \\[§r§(.*)§r§7\\])?$");

    public static GearParseResult parseItemStack(ItemStack itemStack) {
        List<StatActualValue> identifications = new ArrayList<>();
        List<Powder> powders = new ArrayList<>();
        int tierCount = 0;
        int durabilityMax = 0;
        GearTier tier = null;
        GearType gearType = null;

        // Parse lore for identifications, powders and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(LoreUtils.getTooltipLines(itemStack));
        lore.remove(0); // remove item name

        for (Component loreLine : lore) {
            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLine.getString());
            String coded = ComponentUtils.getCoded(loreLine);
            String normalizedCoded = WynnUtils.normalizeBadString(coded);

            // Look for powder
            Matcher powderMatcher = POWDER_PATTERN.matcher(normalizedCoded);
            if (powderMatcher.matches()) {
                int usedSlots = Integer.parseInt(powderMatcher.group(1));
                String codedPowders = powderMatcher.group(3);
                if (codedPowders == null) continue;

                String powderString = codedPowders.replaceAll("[^✹✦❋❉✤]", "");
                if (powderString.length() != usedSlots) {
                    WynntilsMod.warn("Mismatch between powder slot count " + usedSlots + " and actual powder symbols: "
                            + codedPowders + " for " + itemStack.getHoverName().getString());
                    // Fall through and use codedPowfers nevertheless
                }

                codedPowders.chars().forEach(ch -> {
                    Powder powder = Powder.getFromSymbol(Character.toString(ch));
                    powders.add(powder);
                });

                continue;
            }

            // Look for tier and rerolls
            Matcher tierMatcher = TIER_AND_REROLL_PATTERN.matcher(normalizedCoded);
            if (tierMatcher.matches()) {
                String tierString = tierMatcher.group(1);
                tier = GearTier.fromFormattedString(tierString);
                // group 2 is the type of item, like "Raid Reward" or "Item"
                // or "Wand" (the latter only for crafted items)
                String gearTypeString = tierMatcher.group(2);
                // This will return null for everything but crafted gear
                gearType = GearType.fromString(gearTypeString);

                // This is either the rerolls (for re-identified gear), or the
                // current durability (for crafted gear)
                String tierCountString = tierMatcher.group(3);
                tierCount = tierCountString != null ? Integer.parseInt(tierCountString) : 0;

                // If we have a crafted gear, we also have a durability max
                String durabilityMaxString = tierMatcher.group(4);
                durabilityMax = durabilityMaxString != null ? Integer.parseInt(durabilityMaxString) : 0;

                continue;
            }

            // Look for identifications
            Matcher statMatcher = IDENTIFICATION_STAT_PATTERN.matcher(normalizedCoded);
            if (statMatcher.matches()) {
                int value = Integer.parseInt(statMatcher.group(1));
                // group 2 is only present for unidentified gears, as the to-part of the range
                String unit = statMatcher.group(3);
                // group 4 is only present for crafted gear, as the top value for that stat
                String starString = statMatcher.group(5);
                String statDisplayName = statMatcher.group(6);

                StatType type = Models.Stat.fromDisplayName(statDisplayName, unit);
                if (type == null && Skill.isSkill(statDisplayName)) {
                    // Skill bonuses looks like stats when parsing, ignore them
                    continue;
                }
                if (type.showAsInverted()) {
                    // Spell Cost stats are shown as negative, but we store them as positive
                    value = -value;
                }

                int stars = starString == null ? 0 : starString.length();

                identifications.add(new StatActualValue(type, value, stars));
            }
        }

        return new GearParseResult(tier, gearType, identifications, powders, tierCount, durabilityMax);
    }

    public static GearParseResult parseInternalRolls(GearInfo gearInfo, JsonObject itemData) {
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
                    // This can happen for skill point bonus, which used to be variable...
                    Skill skill = Skill.fromApiId(id.replace("POINTS", ""));
                    if (skill != null) continue;

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

        return new GearParseResult(gearInfo.tier(), null, identifications, powders, rerolls, 0);
    }

    private static StatActualValue getStatActualValue(GearInfo gearInfo, StatType statType, int internalRoll) {
        StatPossibleValues possibleValue = gearInfo.getPossibleValues(statType);
        if (possibleValue == null) {
            WynntilsMod.warn("Remote player's " + gearInfo.name() + " claims to have " + statType);
            return null;
        }
        int value = Math.round(possibleValue.baseValue() * (internalRoll / 100f));
        if (value == 0) {
            // If we get to 0, use 1 or -1 instead
            value = (int) Math.signum(possibleValue.baseValue());
        }

        // Negative values can never show stars
        int stars = (value > 0) ? GearCalculator.getStarsFromInternalRoll(internalRoll) : 0;

        return new StatActualValue(statType, value, stars);
    }
}
