/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.components.Models;
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.models.concepts.Element;
import com.wynntils.models.concepts.Powder;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.stats.RecollCalculator;
import com.wynntils.models.gearinfo.GearInfo;
import com.wynntils.models.gearinfo.GearRequirements;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.gearinfo.type.GearMajorId;
import com.wynntils.models.gearinfo.type.GearRestrictions;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListDelimiter;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public final class GearTooltipBuilder {
    private static final Pattern ITEM_TIER =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) Item(?: \\[(?<Rolls>\\d+)])?");
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");
    private static final Pattern RANGE_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)§r§2 to §r§a(\\d+)(%|/3s|/5s| tier)?§r§7 ?(.*)$");


    private GearInfo gearInfo;
    private GearInstance gearInstance;
    private GearItem gearItem;

    private List<Component> topTooltip;
    private List<Component> bottomTooltip;

    private final Map<IdentificationPresentationStyle, List<Component>> middleTooltipCache = new HashMap<>();

    public GearTooltipBuilder() {}

    private GearTooltipBuilder(GearInfo gearInfo, GearItem gearItem) {
        this.gearInfo = gearInfo;
        this.gearItem = gearItem;

        topTooltip = buildTopTooltip();
        bottomTooltip = buildBottomTooltip();
    }

    private GearTooltipBuilder(
            GearInfo gearInfo, GearItem gearItem, List<Component> topTooltip, List<Component> bottomTooltip) {
        this.gearInfo = gearInfo;
        this.gearItem = gearItem;

        this.topTooltip = topTooltip;
        this.bottomTooltip = bottomTooltip;
    }

    public static GearTooltipBuilder fromGearInfo(GearInfo gearProfile) {
        return new GearTooltipBuilder(gearProfile, null);
    }

    public static GearTooltipBuilder fromGearItem(GearItem gearItem) {
        return new GearTooltipBuilder(gearItem.getGearInfo(), gearItem);
    }

    public static GearTooltipBuilder fromItemStack(ItemStack itemStack, GearInfo gearInfo, GearItem gearItem) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        // Skip first line which contains name
        Pair<List<Component>, List<Component>> splittedLore = splitLore(tooltips.subList(1, tooltips.size()), gearInfo);

        return new GearTooltipBuilder(gearInfo, gearItem, splittedLore.a(), splittedLore.b());
    }

    public List<Component> getTooltipLines(IdentificationPresentationStyle style) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());

        // Top and bottom are always constant
        tooltip.addAll(topTooltip);

        // In the middle we have the list of identifications, which is different
        // depending on which decorations are requested
        tooltip.addAll(getMiddleTooltip(style));

        tooltip.addAll(bottomTooltip);

        return ComponentUtils.stripDuplicateBlank(tooltip);
    }

    private static Pair<List<Component>, List<Component>> splitLore(List<Component> lore, GearInfo gearInfo) {
        List<Component> topTooltip = new ArrayList<>();
        List<Component> bottomTooltip = new ArrayList<>();

        List<Component> baseTooltip = topTooltip;

        boolean setBonusIDs = false;
        for (Component loreLine : lore) {
            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLine.getString());

            if (unformattedLoreLine.equals("Set Bonus:")) {
                baseTooltip.add(loreLine);
                setBonusIDs = true;
                continue;
            }

            if (setBonusIDs) {
                baseTooltip.add(loreLine);
                if (unformattedLoreLine.isBlank()) {
                    setBonusIDs = false;
                }
                continue;
            }

            if (unformattedLoreLine.contains("] Powder Slots")) {
                baseTooltip.add(loreLine);
                continue;
            }

            Matcher rerollMatcher = ITEM_TIER.matcher(unformattedLoreLine);
            if (rerollMatcher.find()) {
                baseTooltip.add(loreLine);
                continue;
            }

            if (!isIdLine(loreLine, gearInfo)) {
                baseTooltip.add(loreLine);
                continue;
            }

            // if we've reached this point, we have an id. It should not be stored anywhere
            if (baseTooltip == topTooltip) {
                // switch to bottom part
                baseTooltip = bottomTooltip;
            }
        }

        return Pair.of(topTooltip, bottomTooltip);
    }

    private static boolean isIdLine(Component lore, GearInfo item) {
        // This looks quite messy, but is in effect what we did before
        // FIXME: Clean up?
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());
        Matcher identificationMatcher = ITEM_IDENTIFICATION_PATTERN.matcher(unformattedLoreLine);
        if (identificationMatcher.find()) return true;

        Matcher unidentifiedMatcher = RANGE_PATTERN.matcher(unformattedLoreLine);
        if (unidentifiedMatcher.matches()) return true;

        return false;
    }

    private List<Component> buildTopTooltip() {
        List<Component> baseTooltip = new ArrayList<>();

        // attack speed
        if (gearInfo.fixedStats().attackSpeed().isPresent())
            baseTooltip.add(Component.literal(ChatFormatting.GRAY
                    + gearInfo.fixedStats().attackSpeed().get().getName()));

        baseTooltip.add(Component.literal(""));

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
                baseTooltip.add(damage);
            }

            baseTooltip.add(Component.literal(""));
        }

        int health = gearInfo.fixedStats().healthBuff();
        if (health != 0) {
            MutableComponent healthComp = Component.literal("❤ Health: " + StringUtils.toSignedString(health))
                    .withStyle(ChatFormatting.DARK_RED);
            baseTooltip.add(healthComp);
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
                baseTooltip.add(defense);
            }

            baseTooltip.add(Component.literal(""));
        }

        // requirements
        GearRequirements requirements = gearInfo.requirements();
        if (requirements.quest().isPresent()) {
            baseTooltip.add(getRequirement("Quest Req: " + requirements.quest().get()));
        }
        if (requirements.classType().isPresent()) {
            baseTooltip.add(getRequirement(
                    "Class Req: " + requirements.classType().get().getFullName()));
        }
        if (requirements.level() != 0) {
            baseTooltip.add(getRequirement("Combat Lv. Min: " + requirements.level()));
        }
        if (!requirements.skills().isEmpty()) {
            for (Pair<Skill, Integer> skillRequirement : requirements.skills()) {
                baseTooltip.add(
                        getRequirement(skillRequirement.key().getDisplayName() + " Min: " + skillRequirement.value()));
            }
        }

        // FIXME: Only add if we had requirements
        baseTooltip.add(Component.literal(""));

        // Add delimiter if variables stats will follow
        if (!gearInfo.variableStats().isEmpty()) {
            baseTooltip.add(Component.literal(""));
        }

        return baseTooltip;
    }

    private static MutableComponent getRequirement(String requirementName) {
        MutableComponent requirement;
        requirement = Component.literal("✔ ").withStyle(ChatFormatting.GREEN);
        requirement.append(Component.literal(requirementName).withStyle(ChatFormatting.GRAY));
        return requirement;
    }

    private List<Component> buildBottomTooltip() {
        List<Component> baseTooltip = new ArrayList<>();

        // major ids
        // FIXME: Missing "<+Entropy: >Meteor falls..." which should be in AQUA.
        if (!gearInfo.fixedStats().majorIds().isEmpty()) {
            for (GearMajorId majorId : gearInfo.fixedStats().majorIds()) {
                Stream.of(RenderedStringUtils.wrapTextBySize(majorId.lore(), 150))
                        .forEach(c -> baseTooltip.add(Component.literal(c).withStyle(ChatFormatting.DARK_AQUA)));
            }
            baseTooltip.add(Component.literal(""));
        }

        // powder slots
        if (gearInfo.powderSlots() > 0) {
            if (gearItem == null) {
                baseTooltip.add(Component.literal("[" + gearInfo.powderSlots() + " Powder Slots]")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                MutableComponent powderLine = Component.literal(
                                "[" + gearItem.getPowders().size() + "/" + gearInfo.powderSlots() + "] Powder Slots ")
                        .withStyle(ChatFormatting.GRAY);
                if (!gearItem.getPowders().isEmpty()) {
                    MutableComponent powderList = Component.literal("[");
                    for (Powder p : gearItem.getPowders()) {
                        String symbol = p.getColoredSymbol();
                        if (!powderList.getSiblings().isEmpty()) symbol = " " + symbol;
                        powderList.append(Component.literal(symbol));
                    }
                    powderList.append(Component.literal("]"));
                    powderLine.append(powderList);
                }
                baseTooltip.add(powderLine);
            }
        }

        // tier & rerolls
        GearTier gearTier = gearInfo.tier();
        MutableComponent tier = Component.literal(gearTier.getName() + " Item").withStyle(gearTier.getChatFormatting());
        if (gearItem != null && gearItem.getRerolls() > 1) {
            tier.append(" [" + gearItem.getRerolls() + "]");
        }
        baseTooltip.add(tier);

        // untradable
        if (gearInfo.metaInfo().restrictions() != GearRestrictions.NONE) {
            baseTooltip.add(Component.literal(StringUtils.capitalizeFirst(
                            gearInfo.metaInfo().restrictions().getDescription() + " Item"))
                    .withStyle(ChatFormatting.RED));
        }

        Optional<String> lore = gearInfo.metaInfo().lore();
        if (lore.isPresent()) {
            Stream.of(RenderedStringUtils.wrapTextBySize(lore.get(), 150))
                    .forEach(c -> baseTooltip.add(Component.literal(c).withStyle(ChatFormatting.DARK_GRAY)));
        }

        return baseTooltip;
    }

    private Component getHoverName() {
        String prefix = gearItem != null && gearItem.isUnidentified() ? Models.GearItem.UNIDENTIFIED_PREFIX : "";

        return Component.literal(prefix + gearInfo.name())
                .withStyle(gearInfo.tier().getChatFormatting());
    }

    private List<Component> getMiddleTooltip(IdentificationPresentationStyle style) {
//        List<Component> tooltips = middleTooltipCache.get(style);
 //       if (tooltips != null) return tooltips;
        List<Component> tooltips;
        tooltips = buildMiddleTooltipNew(style);
     //   middleTooltipCache.put(style, tooltips);
        return tooltips;
    }

    private List<Component> buildMiddleTooltipNew(IdentificationPresentationStyle style) {
        List<Component> allStatLines = new ArrayList<>();
        GearInstance gearInstance = gearItem == null ? null : gearItem.getGearInstance();
        List<StatActualValue> stats = gearInstance != null ? gearInstance.getIdentifications() : List.of();

        GearInfo gearInfo = Models.GearInfo.getGearInfo(this.gearInfo.name());

        List<Pair<Skill, Integer>> skillBonuses = gearInfo.fixedStats().skillBonuses();
        for (Skill skill : Skill.getGearSkillOrder()) {
            Pair<Skill, Integer> skillBonusValue = getSkillBonuses(skill, skillBonuses);
            if (skillBonusValue == null) continue;

            Component line = buildIdLoreLineSkillBonusNew(style.decorations(), skillBonusValue);
            allStatLines.add(line);
        }
        if (!skillBonuses.isEmpty()) {
            allStatLines.add(Component.literal(""));
        }

        List<StatType> listOrdering = getStatListOrdering();
        List<StatType> allStats = gearInfo.getVariableStats();

        boolean useDelimiter = ItemStatInfoFeature.INSTANCE.groupIdentifications;

        boolean delimiterNeeded = false;
        // We need to iterate over all possible stats in order, to be able
        // to inject delimiters, instead of just using Models.Stat.getSortedStats
        for (StatType statType : listOrdering) {
            if (useDelimiter && statType instanceof StatListDelimiter) {
                if (delimiterNeeded) {
                    allStatLines.add(Component.literal(""));
                    delimiterNeeded = false;
                }
            }
            // Most stat types are probably not valid for this gear
            if (!allStats.contains(statType)) continue;

            Component line;
            if (gearInstance != null) {
                // Put in actual value
                StatActualValue statActualValue = gearInstance.getActualValue(statType);
                if (statActualValue == null) continue;

                line = buildIdLoreLineNew(gearInfo, style.decorations(), statActualValue);
            } else {
                // Put in range of possible values
                StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);
                line = buildIdLoreLineRangedNew(gearInfo, style.decorations(), possibleValues);
            }
            allStatLines.add(line);
            delimiterNeeded = true;
        }

        return allStatLines;
    }

    private static List<StatType> getStatListOrdering() {
        // FIXME: introduce enum to select order!
        List<StatType> order;
        if (ItemStatInfoFeature.INSTANCE.reorderIdentifications) {
            order = Models.Stat.defaultOrder;
        } else {
            order = Models.Stat.wynntilsOrder;
        }
        return order;
    }

    private Pair<Skill, Integer> getSkillBonuses(Skill skill, List<Pair<Skill, Integer>> skillBonuses) {
        for (Pair<Skill, Integer> skillBonusValue : skillBonuses) {
            if (skillBonusValue.key() == skill) {
                return skillBonusValue;
            }
        }

        return null;
    }

    private Component buildIdLoreLineSkillBonusNew(
            IdentificationDecorations decorations, Pair<Skill, Integer> skillBonus) {
        String inGameName = skillBonus.key().getDisplayName();
        int value = skillBonus.value();
        StatUnit unitType = StatUnit.RAW;
        MutableComponent baseComponent = buildBaseComponentNew(inGameName, value, unitType);

        return baseComponent;
    }

    private Component buildIdLoreLineRangedNew(
            GearInfo gearInfo, IdentificationDecorations decorations, StatPossibleValues possibleValues) {
        String inGameName = possibleValues.stat().getDisplayName();
        StatUnit unitType = possibleValues.stat().getUnit();
        boolean invert = possibleValues.stat().isInverted();

        MutableComponent baseComponent =
                buildBaseComponentRangedNew(inGameName, possibleValues.range(), unitType, invert);
        baseComponent.append(" #");
        return baseComponent;
    }

    private Component buildIdLoreLineNew(
            GearInfo gearInfo, IdentificationDecorations decorations, StatActualValue actualValue) {
        StatType statType = actualValue.stat();
        StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);

        /*
        Note: negative values will never show stars!
        See https://forums.wynncraft.com/threads/stats-and-identifications-guide.246308/
                 */
        String starString = ItemStatInfoFeature.INSTANCE.showStars ? "***".substring(3 - actualValue.stars()) : "";

        MutableComponent baseComponent = buildBaseComponentNew(
                statType.getDisplayName(), actualValue.value(), statType.getUnit(), statType.isInverted(), starString);

        if (possibleValues.range().isFixed()) return baseComponent;

        switch (decorations) {
            case PERCENT -> appendPercentLoreLine(baseComponent, actualValue, possibleValues);
            case RANGE -> appendRangeLoreLine(baseComponent, actualValue, possibleValues);
            case REROLL_CHANCE -> appendRerollLoreLine(baseComponent, actualValue, possibleValues);
        }

        // FIXME: at some point, remove marker
        baseComponent.append(" #");

        return baseComponent;
    }

    private MutableComponent buildBaseComponentRangedNew(
            String inGameName, RangedValue value, StatUnit unitType, boolean invert) {
        String unit = unitType.getDisplayName();
        // Use value.low as representative; assume both high and low are either < or > 0.
        boolean isGood = invert ? (value.low() < 0) : (value.low() > 0);
        ChatFormatting colorCode = isGood ? ChatFormatting.GREEN : ChatFormatting.RED;
        ChatFormatting colorCodeDark = isGood ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED;

        MutableComponent baseComponent = Component.literal("");

        // FIXME: make config
        // FIXME: check with spell cost
        // FIXME: Range should always be "low" is worst and "high" is best.
        boolean showBestValueLast = true; // false is vanilla behavior
        if (showBestValueLast) {
            if (invert) {
                int first = value.high();
                int last = value.low();
            } else {
                int first = value.low();
                int last = value.high();
            }
        } else {
            // Emulate Wynncraft behavior
            if (value.low() < 0) {
                // Show the value closest to zero first
                int first = value.high();
                int last = value.low();
            } else {
                int first = value.low();
                int last = value.high();
            }
        }
        baseComponent.append(Component.literal(StringUtils.toSignedString(value.low())).withStyle(colorCode));
        baseComponent.append(Component.literal(" to ").withStyle(colorCodeDark));
        baseComponent.append(Component.literal(value.high() + unit).withStyle(colorCode));

        baseComponent.append(Component.literal(" " + inGameName).withStyle(ChatFormatting.GRAY));

        return baseComponent;
    }

    private MutableComponent buildBaseComponentNew(
            String inGameName, int value, StatUnit unitType, boolean invert, String stars) {
        String unit = unitType.getDisplayName();

        MutableComponent baseComponent = Component.literal("");

        MutableComponent statInfo = Component.literal((value > 0 ? "+" : "") + value + unit);
        boolean isGood = invert ? (value < 0) : (value > 0);
        statInfo.setStyle(Style.EMPTY.withColor(isGood ? ChatFormatting.GREEN : ChatFormatting.RED));

        baseComponent.append(statInfo);

        if (!stars.isEmpty()) {
            baseComponent.append(Component.literal(stars).withStyle(ChatFormatting.DARK_GREEN));
        }

        baseComponent.append(Component.literal(" " + inGameName).withStyle(ChatFormatting.GRAY));

        return baseComponent;
    }

    private MutableComponent buildBaseComponentNew(String inGameName, int value, StatUnit unitType) {
        return buildBaseComponentNew(inGameName, value, unitType, false, "");
    }

    private Component appendPercentLoreLine(
            MutableComponent baseComponent, StatActualValue actualValue, StatPossibleValues possibleValues) {
        // calculate percent/range/reroll chances, append to lines
        int min = possibleValues.range().low();
        int max = possibleValues.range().high();

        float percentage = MathUtils.inverseLerp(min, max, actualValue.value()) * 100;
        MutableComponent percentageTextComponent = ColorScaleUtils.getPercentageTextComponent(
                percentage, ItemStatInfoFeature.INSTANCE.colorLerp, ItemStatInfoFeature.INSTANCE.decimalPlaces);

        baseComponent.append(percentageTextComponent);
        return baseComponent;
    }

    private Component appendRangeLoreLine(
            MutableComponent baseComponent, StatActualValue actualValue, StatPossibleValues possibleValues) {
        // calculate percent/range/reroll chances, append to lines
        int min = possibleValues.range().low();
        int max = possibleValues.range().high();

        MutableComponent rangeTextComponent = Component.literal(" [")
                .append(Component.literal(min + ", " + max).withStyle(ChatFormatting.GREEN))
                .append("]")
                .withStyle(ChatFormatting.DARK_GREEN);

        baseComponent.append(rangeTextComponent);

        return baseComponent;
    }

    private Component appendRerollLoreLine(
            MutableComponent baseComponent, StatActualValue actualValue, StatPossibleValues possibleValues) {
        RecollCalculator chances = RecollCalculator.calculateChances(possibleValues, actualValue);

        MutableComponent rerollChancesComponent = Component.literal(
                        String.format(Locale.ROOT, " \u2605%.2f%%", chances.getPerfect() * 100))
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format(Locale.ROOT, " \u21E7%.1f%%", chances.getIncrease() * 100))
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(String.format(Locale.ROOT, " \u21E9%.1f%%", chances.getDecrease() * 100))
                        .withStyle(ChatFormatting.RED));

        baseComponent.append(rerollChancesComponent);

        return baseComponent;
    }

    public enum IdentificationDecorations {
        PERCENT,
        RANGE,
        REROLL_CHANCE
    }

    public record IdentificationPresentationStyle(
            IdentificationDecorations decorations, boolean reorder, boolean group) {}
}
