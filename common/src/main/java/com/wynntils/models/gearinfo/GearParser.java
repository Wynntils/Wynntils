/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.concepts.Powder;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.WynnItemMatchers;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class GearParser {
    // FIXME: Clean up this class!
    public static final Pattern ID_NEW_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)(%|/3s|/5s| tier)?(?:§r§2(\\*{1,3}))? ?§r§7 ?(.*)$");
    private static final Pattern REROLL_PATTERN =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) "
                    + "(Raid Reward|Item)(?: \\[(?<Rolls>\\d+)])?");

    public static final Pattern VARIABLE_STAT_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)(%|/3s|/5s| tier)?(?:§r§2(\\*{1,3}))? ?§r§7 ?(.*)$");

    public GearInstance fromItemStack(GearInfo gearInfo, ItemStack itemStack) {
        List<StatActualValue> identifications = new ArrayList<>();
        List<Powder> powders = List.of();
        int rerolls = 0;

        // Parse lore for identifications, powders and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(LoreUtils.getTooltipLines(itemStack));
        lore.remove(0); // remove item name

        for (Component loreLine : lore) {
            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLine.getString());

            // Look for Powder
            if (unformattedLoreLine.contains("] Powder Slots")) {
                powders = Powder.findPowders(unformattedLoreLine);
                continue;
            }

            // Look for Rerolls
            Optional<Integer> rerollOpt = getRerollCount(loreLine);
            if (rerollOpt.isPresent()) {
                rerolls = rerollOpt.get();
                continue;
            }

            // Look for identifications
            String formatId = ComponentUtils.getCoded(loreLine);
            Matcher statMatcher = VARIABLE_STAT_PATTERN.matcher(formatId);
            if (statMatcher.matches()) {
                int value = Integer.parseInt(statMatcher.group(2));
                String unit = statMatcher.group(3);
                String statDisplayName = statMatcher.group(5);
                String starString = statMatcher.group(4);
                int stars = starString == null ? 0 : starString.length();

                StatType type = Models.Stat.fromDisplayName(statDisplayName, unit);
                if (type == null && Skill.isSkill(statDisplayName)) {
                    // Skill point buff looks like stats when parsing
                    continue;
                }
                if (type.showAsInverted()) {
                    // Spell Cost stats are shown as negative, but we store them as positive
                    value = -value;
                }

                identifications.add(new StatActualValue(type, value, stars));
            }
        }

        return GearInstance.create(gearInfo, identifications, powders, rerolls);
    }

    private Optional<Integer> getRerollCount(Component lore) {
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());

        Matcher rerollMatcher = REROLL_PATTERN.matcher(unformattedLoreLine);
        if (!rerollMatcher.find()) return Optional.empty();

        if (rerollMatcher.group("Rolls") != null) {
            return Optional.of(Integer.parseInt(rerollMatcher.group("Rolls")));
        } else {
            return Optional.of(0);
        }
    }

    /*
    private static final Pattern RANGE_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)§r§2 to §r§a(\\d+)(%|/3s|/5s| tier)?§r§7 ?(.*)$");

            // Range pattern will normally not happen...
            Matcher id3Matcher = RANGE_PATTERN.matcher(formatId);
            if (id3Matcher.matches()) {
                boolean isNegative = id3Matcher.group(1).charAt(0) == 'c';
                int value = Integer.parseInt(id3Matcher.group(2));
                int valueMax = Integer.parseInt(id3Matcher.group(3));
                String idName = id3Matcher.group(5);
                String unitMatch = id3Matcher.group(4);
                String unit = unitMatch == null ? "" : unitMatch;

                StatType type = Models.Stat.fromDisplayName(idName, unit);
                if (type == null && Skill.isSkill(idName)) {
                    // Skill point buff looks like stats when parsing
                    // FIXME: Handle
                }
            }

     */

    public GearInstance fromIngameItemData(GearInfo gearInfo, JsonObject itemData) {
        List<StatActualValue> identifications = new ArrayList<>();

        // Lore lines is: type: "LORETYPE", percent: <number>, where 100 is baseline, so can be > 100 and < 100.
        if (itemData.has("identifications")) {
            JsonArray ids = itemData.getAsJsonArray("identifications");
            for (int i = 0; i < ids.size(); i++) {
                JsonObject idInfo = ids.get(i).getAsJsonObject();
                String id = idInfo.get("type").getAsString();
                int intPercent = idInfo.get("percent").getAsInt();

                // Convert e.g. DAMAGEBONUS to our StatTypes
                StatType statType = Models.Stat.fromLoreId(id);
                if (statType == null) {
                    // FIXME: do not warn if this is skill point
                    // This can happen for skill point bonus, which used to be variable...
                    WynntilsMod.warn("Remote player's " + gearInfo.name() + " contains unknown stat type " + id);
                    continue;
                }

                StatPossibleValues possibleValue = gearInfo.getPossibleValues(statType);
                if (possibleValue == null) {
                    WynntilsMod.warn("Remote player's " + gearInfo.name() + " claims to have " + statType);
                    continue;
                }
                int value = Math.round(possibleValue.baseValue() * (intPercent / 100f));

                // account for mistaken rounding
                if (value == 0) {
                    value = 1;
                }

                int stars = GearCalculator.getStarsFromPercent(intPercent);
                // FIXME: Negative values should never show stars!

                identifications.add(new StatActualValue(statType, value, stars));
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

        return GearInstance.create(gearInfo, identifications, powders, rerolls);
    }

    public CraftedGearItem getCraftedGearItem(ItemStack itemStack) {
        CappedValue durability = WynnItemMatchers.getDurability(itemStack);

        List<StatActualValue> identifications = new ArrayList<>();
        List<Powder> powders = List.of();

        // Parse lore for identifications and powders
        List<Component> lore = ComponentUtils.stripDuplicateBlank(itemStack.getTooltipLines(null, TooltipFlag.NORMAL));
        if (lore.size() <= 1) {
            // We should always have the item name as the first line, unless some other mod interacts badly...
            return null;
        }
        lore.remove(0); // remove item name

        for (Component loreLine : lore) {
            // FIXME: This is partially shared with GearAnnotator
            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLine.getString());

            // Look for Powder
            if (unformattedLoreLine.contains("] Powder Slots")) {
                powders = Powder.findPowders(unformattedLoreLine);
                continue;
            }

            // Look for identifications
            String formatId = ComponentUtils.getCoded(loreLine);
            Matcher statMatcher = ID_NEW_PATTERN.matcher(formatId);
            if (statMatcher.matches()) {
                int value = Integer.parseInt(statMatcher.group(2));
                String unit = statMatcher.group(3);
                String statDisplayName = statMatcher.group(5);

                StatType type = Models.Stat.fromDisplayName(statDisplayName, unit);
                if (type == null && Skill.isSkill(statDisplayName)) {
                    // Skill point buff looks like stats when parsing
                    continue;
                }

                // FIXME: crafted gear do not have stars, ever
                // Instead, they have <current value><unit>/<max value><unit>
                // Also, "fixed" stats can become changing here...
                // Also, the order of stats is completely arbitrary
                // So we need a better design to fit this

                identifications.add(new StatActualValue(type, value, -1));
            }
        }

        // FIXME: Missing requirements and damages
        return new CraftedGearItem(List.of(), List.of(), identifications, powders, durability);
    }
}
