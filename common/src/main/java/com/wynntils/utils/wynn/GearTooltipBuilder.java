/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.components.Models;
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.models.concepts.DamageType;
import com.wynntils.models.concepts.Element;
import com.wynntils.models.concepts.Powder;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gear.GearIdentificationContainer;
import com.wynntils.models.gear.GearInstance;
import com.wynntils.models.gear.ReidentificationChances;
import com.wynntils.models.gear.profile.GearProfile;
import com.wynntils.models.gear.profile.IdentificationProfile;
import com.wynntils.models.gear.profile.MajorIdentification;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.IdentificationModifier;
import com.wynntils.models.gear.type.RequirementType;
import com.wynntils.models.gearinfo.GearInfo;
import com.wynntils.models.gearinfo.type.GearDamageType;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.FixedStats;
import com.wynntils.models.stats.StatOrder;
import com.wynntils.models.stats.type.StatActualValue;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public class GearTooltipBuilder {
    private static final Pattern ITEM_TIER =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) Item(?: \\[(?<Rolls>\\d+)])?");
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    private GearProfile gearProfile;
    private GearItem gearItem;

    private List<Component> topTooltip;
    private List<Component> bottomTooltip;

    private final Map<IdentificationPresentationStyle, List<Component>> middleTooltipCache = new HashMap<>();

    public GearTooltipBuilder() {}

    private GearTooltipBuilder(GearProfile gearProfile, GearItem gearItem) {
        this.gearProfile = gearProfile;
        this.gearItem = gearItem;

        topTooltip = buildTopTooltip();
        bottomTooltip = buildBottomTooltip();
    }

    private GearTooltipBuilder(
            GearProfile gearProfile, GearItem gearItem, List<Component> topTooltip, List<Component> bottomTooltip) {
        this.gearProfile = gearProfile;
        this.gearItem = gearItem;

        this.topTooltip = topTooltip;
        this.bottomTooltip = bottomTooltip;
    }

    public static GearTooltipBuilder fromGearProfile(GearProfile gearProfile) {
        return new GearTooltipBuilder(gearProfile, null);
    }

    public static GearTooltipBuilder fromGearItem(GearItem gearItem) {
        return new GearTooltipBuilder(gearItem.getGearProfile(), gearItem);
    }

    public static GearTooltipBuilder fromItemStackNew(ItemStack itemStack, GearProfile gearProfile, GearItem gearItem) {
        List<Component> tooltips = new ArrayList<>();
        GearInfo gearInfo = Models.GearInfo.getGearInfo(gearProfile.getDisplayName());

        // FIXED STATS
        // Attack speed
        FixedStats fixedStats = gearInfo.fixedStats();
        Optional<GearAttackSpeed> attackSpeed = fixedStats.attackSpeed();
        if (attackSpeed.isPresent()) {
            tooltips.add(Component.literal(attackSpeed.get().getName()));
        }
        tooltips.add(Component.literal(""));

        // Health
        if (fixedStats.healthBuff() != 0) {
            tooltips.add(Component.literal("❤ Health: " + StringUtils.toSignedString(fixedStats.healthBuff()))
                    .withStyle(ChatFormatting.DARK_RED));
            tooltips.add(Component.literal(""));
        }

        // Defences
        for (Pair<Element, Integer> defenseValue : fixedStats.defences()) {
            tooltips.add(Component.literal(defenseValue.key().getSymbol() + " "
                            + defenseValue.key().getDisplayName())
                    .withStyle(defenseValue.key().getColorCode())
                    .append(Component.literal(" Defence: " + StringUtils.toSignedString(defenseValue.value()))
                            .withStyle(ChatFormatting.GRAY)));
        }

        // Damages
        for (Pair<GearDamageType, RangedValue> damageValue : fixedStats.damages()) {
            GearDamageType damageType = damageValue.key();
            tooltips.add(Component.literal(damageType.getSymbol() + " " + damageType.getDisplayName())
                    .withStyle(damageType.getColorCode())
                    .append(Component.literal(" Damage: " + damageValue.value().asString())
                            .withStyle(damageType.getColorCode())));

            // : ChatFormatting.GRAY)); // neutral is all gold ???
        }

        if (!fixedStats.damages().isEmpty()) {
            tooltips.add(Component.literal("Average DPS: ???"));
        }
        tooltips.add(Component.literal(""));

        return new FixedTB(tooltips);
    }

    public static class FixedTB extends GearTooltipBuilder {
        private List<Component> tooltip;

        public FixedTB(List<Component> tooltip) {
            this.tooltip = tooltip;
        }

        @Override
        public List<Component> getTooltipLines(IdentificationPresentationStyle style) {
            return tooltip;
        }
    }

    public static GearTooltipBuilder fromItemStack(ItemStack itemStack, GearProfile gearProfile, GearItem gearItem) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        // Skip first line which contains name
        Pair<List<Component>, List<Component>> splittedLore =
                splitLore(tooltips.subList(1, tooltips.size()), gearProfile);

        return new GearTooltipBuilder(gearProfile, gearItem, splittedLore.a(), splittedLore.b());
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

    private static Pair<List<Component>, List<Component>> splitLore(List<Component> lore, GearProfile gearProfile) {
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

            if (!isIdLine(loreLine, gearProfile)) {
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

    private static boolean isIdLine(Component lore, GearProfile item) {
        // This looks quite messy, but is in effect what we did before
        // FIXME: Clean up?
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());
        Matcher identificationMatcher = ITEM_IDENTIFICATION_PATTERN.matcher(unformattedLoreLine);
        if (!identificationMatcher.find()) return false; // not a valid id line

        return true;
    }

    private List<Component> buildTopTooltip() {
        List<Component> baseTooltip = new ArrayList<>();

        // attack speed
        if (gearProfile.getAttackSpeed() != null)
            baseTooltip.add(Component.literal(gearProfile.getAttackSpeed().asLore()));

        baseTooltip.add(Component.literal(""));

        // elemental damages
        if (!gearProfile.getDamageTypes().isEmpty()) {
            Map<DamageType, String> damages = gearProfile.getDamages();
            for (Map.Entry<DamageType, String> entry : damages.entrySet()) {
                DamageType type = entry.getKey();
                MutableComponent damage =
                        Component.literal(type.getSymbol() + " " + type).withStyle(type.getColor());
                damage.append(Component.literal(" Damage: " + entry.getValue())
                        .withStyle(
                                type == DamageType.NEUTRAL
                                        ? type.getColor()
                                        : ChatFormatting.GRAY)); // neutral is all gold
                baseTooltip.add(damage);
            }

            baseTooltip.add(Component.literal(""));
        }

        // elemental defenses
        if (!gearProfile.getDefenseTypes().isEmpty()) {
            int health = gearProfile.getHealth();
            if (health != 0) {
                MutableComponent healthComp = Component.literal("❤ Health: " + StringUtils.toSignedString(health))
                        .withStyle(ChatFormatting.DARK_RED);
                baseTooltip.add(healthComp);
            }

            Map<DamageType, Integer> defenses = gearProfile.getElementalDefenses();
            for (Map.Entry<DamageType, Integer> entry : defenses.entrySet()) {
                DamageType type = entry.getKey();
                MutableComponent defense =
                        Component.literal(type.getSymbol() + " " + type).withStyle(type.getColor());
                defense.append(Component.literal(" Defence: " + StringUtils.toSignedString(entry.getValue()))
                        .withStyle(ChatFormatting.GRAY));
                baseTooltip.add(defense);
            }

            baseTooltip.add(Component.literal(""));
        }

        // requirements
        if (gearProfile.hasRequirements()) {
            Map<RequirementType, String> requirements = gearProfile.getRequirements();
            // fire, water, air, thunder, earth
            for (Map.Entry<RequirementType, String> entry : requirements.entrySet()) {
                RequirementType type = entry.getKey();
                MutableComponent requirement;

                requirement = Component.literal("✔ ").withStyle(ChatFormatting.GREEN);
                requirement.append(
                        Component.literal(type.asLore() + entry.getValue()).withStyle(ChatFormatting.GRAY));
                baseTooltip.add(requirement);
            }

            baseTooltip.add(Component.literal(""));
        }

        // ids
        if (!gearProfile.getStatuses().isEmpty()) {
            baseTooltip.add(Component.literal(""));
        }

        return baseTooltip;
    }

    private List<Component> buildBottomTooltip() {
        List<Component> baseTooltip = new ArrayList<>();

        // major ids
        if (gearProfile.getMajorIds() != null && !gearProfile.getMajorIds().isEmpty()) {
            for (MajorIdentification majorId : gearProfile.getMajorIds()) {
                Stream.of(RenderedStringUtils.wrapTextBySize(majorId.asLore(), 150))
                        .forEach(c -> baseTooltip.add(Component.literal(c).withStyle(ChatFormatting.DARK_AQUA)));
            }
            baseTooltip.add(Component.literal(""));
        }

        // powder slots
        if (gearProfile.getPowderAmount() > 0) {
            if (gearItem == null) {
                baseTooltip.add(Component.literal("[" + gearProfile.getPowderAmount() + " Powder Slots]")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                MutableComponent powderLine = Component.literal(
                                "[" + gearItem.getPowders().size() + "/" + gearProfile.getPowderAmount()
                                        + "] Powder Slots ")
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
        MutableComponent tier = gearProfile.getTier().asLore().copy();
        if (gearItem != null && gearItem.getRerolls() > 1) {
            tier.append(" [" + gearItem.getRerolls() + "]");
        }
        baseTooltip.add(tier);

        // untradable
        if (gearProfile.getRestriction() != null) {
            baseTooltip.add(Component.literal(StringUtils.capitalizeFirst(gearProfile.getRestriction() + " Item"))
                    .withStyle(ChatFormatting.RED));
        }

        String lore = gearProfile.getLore();
        if (lore != null) {
            Stream.of(RenderedStringUtils.wrapTextBySize(lore, 150))
                    .forEach(c -> baseTooltip.add(Component.literal(c).withStyle(ChatFormatting.DARK_GRAY)));
        }

        return baseTooltip;
    }

    private Component getHoverName() {
        String prefix = gearItem != null && gearItem.isUnidentified() ? Models.GearItem.UNIDENTIFIED_PREFIX : "";

        return Component.literal(prefix + gearProfile.getDisplayName())
                .withStyle(gearProfile.getTier().getChatFormatting());
    }

    private List<Component> getMiddleTooltip(IdentificationPresentationStyle style) {
        //     List<Component> tooltips = middleTooltipCache.get(style);
        //        if (tooltips != null) return tooltips;

        List<Component> tooltips;
        tooltips = buildMiddleTooltipNew(style);
        //     middleTooltipCache.put(style, tooltips);
        return tooltips;
    }

    private List<Component> buildTopTooltipNew(IdentificationPresentationStyle style) {
        List<Component> allStatLines = new ArrayList<>();
        GearInfo gearInfo = Models.GearInfo.getGearInfo(gearProfile.getDisplayName());

        List<Pair<Element, Integer>> defences = gearInfo.fixedStats().defences();
        for (Element element : Element.values()) {
            Pair<Element, Integer> defenceValue = getElementDefence(element, defences);
            if (defenceValue == null) continue;

            Component line = buildIdLoreLineDefenceNew(style.decorations(), defenceValue);
            allStatLines.add(line);
        }

        return allStatLines;
    }

    private List<Component> buildMiddleTooltipNew(IdentificationPresentationStyle style) {
        List<Component> allStatLines = new ArrayList<>();
        GearInstance gearInstance = gearItem.getGearInstance();
        List<StatActualValue> stats = gearInstance.getIdentifications();

        GearInfo gearInfo = Models.GearInfo.getGearInfo(gearProfile.getDisplayName());

        List<Pair<Skill, Integer>> skillBuffs = gearInfo.fixedStats().skillBuffs();
        for (Skill skill : getSkillOrder()) {
            Pair<Skill, Integer> skillBuffValue = getSkillBuffs(skill, skillBuffs);
            if (skillBuffValue == null) continue;

            Component line = buildIdLoreLineSkillBuffNew(style.decorations(), skillBuffValue);
            allStatLines.add(line);
        }
        if (!skillBuffs.isEmpty()) {
            allStatLines.add(Component.literal(""));
        }

        for (String apiName : StatOrder.getWynncraftOrder()) {
            List<StatType> statKinds = Models.Stat.fromApiName(apiName);
            for (StatType statKind : statKinds) {
                StatActualValue statActualValue = getStatOfKind(statKind, stats);
                if (statActualValue == null) continue;

                Component line = buildIdLoreLineNew(gearInfo, style.decorations(), statActualValue);
                allStatLines.add(line);
            }
        }

        return allStatLines;
    }

    private List<Skill> getSkillOrder() {
        return List.of(Skill.STRENGTH, Skill.DEXTERITY, Skill.INTELLIGENCE, Skill.AGILITY, Skill.DEFENCE);
    }

    private Pair<Skill, Integer> getSkillBuffs(Skill skill, List<Pair<Skill, Integer>> skillBuffs) {
        for (Pair<Skill, Integer> skillBuffValue : skillBuffs) {
            if (skillBuffValue.key().equals(skill)) {
                return skillBuffValue;
            }
        }

        return null;
    }

    private Pair<Element, Integer> getElementDefence(Element element, List<Pair<Element, Integer>> defences) {
        for (Pair<Element, Integer> defenceValue : defences) {
            if (defenceValue.key().equals(element)) {
                return defenceValue;
            }
        }

        return null;
    }

    private StatActualValue getStatOfKind(StatType statKind, List<StatActualValue> stats) {
        for (StatActualValue stat : stats) {
            if (stat.stat().equals(statKind)) {
                return stat;
            }
        }

        return null;
    }

    private List<Component> buildMiddleTooltip(IdentificationPresentationStyle style) {
        List<GearIdentificationContainer> idContainers;
        if (gearItem == null || gearItem.isUnidentified()) {
            idContainers = WynnItemUtils.identificationsFromProfile(gearProfile);
        } else {
            idContainers = gearItem.getIdContainers();
        }

        if (idContainers.isEmpty()) {
            return List.of();
        }

        Map<String, Component> map = idContainers.stream()
                .collect(Collectors.toMap(
                        GearIdentificationContainer::shortIdName,
                        idContainer -> buildIdLoreLine(style.decorations(), idContainer)));

        if (style.reorder()) {
            return Models.GearProfiles.orderComponents(map, style.group());
        } else {
            return new ArrayList<>(map.values());
        }
    }

    private Component buildIdLoreLineSkillBuffNew(
            IdentificationDecorations decorations, Pair<Skill, Integer> skillBuff) {
        String inGameName = skillBuff.key().getDisplayName();
        int value = skillBuff.value();
        StatUnit unitType = StatUnit.RAW;
        MutableComponent baseComponent = buildBaseComponentNew(inGameName, value, unitType);

        return baseComponent;
    }

    private Component buildIdLoreLineDefenceNew(
            IdentificationDecorations decorations, Pair<Element, Integer> gearIdentification) {
        String inGameName = gearIdentification.key().getDisplayName() + " Defence";
        int value = gearIdentification.value();
        StatUnit unitType = StatUnit.RAW;
        MutableComponent baseComponent = buildBaseComponentNew(inGameName, value, unitType);
        // FIXME: for now, just do baseComponent
        return baseComponent;
    }

    private Component buildIdLoreLineNew(
            GearInfo gearInfo, IdentificationDecorations decorations, StatActualValue statActualValue) {
        String inGameName = statActualValue.stat().displayName();
        int value = statActualValue.value();
        StatUnit unitType = statActualValue.stat().unit();
        boolean invert = Models.Stat.isSpellStat(statActualValue.stat());

        String starString = ItemStatInfoFeature.INSTANCE.showStars ? "***".substring(3 - statActualValue.stars()) : "";

        MutableComponent baseComponent = buildBaseComponentNew(inGameName, value, unitType, invert, starString);
        baseComponent.append(" #");
        // FIXME: for now, just do baseComponent
        return baseComponent;
    }

    private Component buildIdLoreLine(IdentificationDecorations decorations, GearIdentificationContainer idContainer) {
        MutableComponent baseComponent = buildBaseComponent(idContainer);
        if (idContainer.idProfile().hasConstantValue()) return baseComponent;

        return switch (decorations) {
            case PERCENT -> appendPercentLoreLine(baseComponent, idContainer);
            case RANGE -> appendRangeLoreLine(baseComponent, idContainer);
            case REROLL_CHANCE -> appendRerollLoreLine(baseComponent, idContainer);
        };
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

    private MutableComponent buildBaseComponent(GearIdentificationContainer idContainer) {
        boolean isInverted = idContainer.idProfile() != null
                ? idContainer.idProfile().isInverted()
                : Models.GearProfiles.getIdentificationOrderer().isInverted(idContainer.shortIdName());

        IdentificationModifier type = idContainer.idProfile() != null
                ? idContainer.idProfile().getType()
                : IdentificationProfile.getTypeFromName(idContainer.shortIdName());
        if (type == null) return null; // not a valid id

        String unit = type.getInGame(idContainer.shortIdName());

        int value = idContainer.value();
        MutableComponent baseComponent = Component.literal("");

        MutableComponent statInfo = Component.literal((value > 0 ? "+" : "") + value + unit);
        statInfo.setStyle(Style.EMPTY.withColor(isInverted ^ (value > 0) ? ChatFormatting.GREEN : ChatFormatting.RED));

        baseComponent.append(statInfo);

        if (ItemStatInfoFeature.INSTANCE.showStars)
            baseComponent.append(
                    Component.literal("***".substring(3 - idContainer.stars())).withStyle(ChatFormatting.DARK_GREEN));

        baseComponent.append(Component.literal(" " + idContainer.inGameIdName()).withStyle(ChatFormatting.GRAY));

        return baseComponent;
    }

    private Component appendPercentLoreLine(MutableComponent baseComponent, GearIdentificationContainer idContainer) {
        IdentificationProfile idProfile = idContainer.idProfile();

        // calculate percent/range/reroll chances, append to lines
        int min = idProfile.getMin();
        int max = idProfile.getMax();

        float percentage = MathUtils.inverseLerp(min, max, idContainer.value()) * 100;
        MutableComponent percentageTextComponent = ColorScaleUtils.getPercentageTextComponent(
                percentage, ItemStatInfoFeature.INSTANCE.colorLerp, ItemStatInfoFeature.INSTANCE.decimalPlaces);

        baseComponent.append(percentageTextComponent);
        return baseComponent;
    }

    private Component appendRangeLoreLine(MutableComponent baseComponent, GearIdentificationContainer idContainer) {
        IdentificationProfile idProfile = idContainer.idProfile();

        // calculate percent/range/reroll chances, append to lines
        int min = idProfile.getMin();
        int max = idProfile.getMax();

        MutableComponent rangeTextComponent = Component.literal(" [")
                .append(Component.literal(min + ", " + max).withStyle(ChatFormatting.GREEN))
                .append("]")
                .withStyle(ChatFormatting.DARK_GREEN);

        baseComponent.append(rangeTextComponent);

        return baseComponent;
    }

    private Component appendRerollLoreLine(MutableComponent baseComponent, GearIdentificationContainer idContainer) {
        IdentificationProfile idProfile = idContainer.idProfile();

        ReidentificationChances chances =
                ReidentificationChances.calculateChances(idProfile, idContainer.value(), idContainer.stars());

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
