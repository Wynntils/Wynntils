/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.models.concepts.DamageType;
import com.wynntils.models.concepts.Element;
import com.wynntils.models.concepts.Powder;
import com.wynntils.models.gear.GearIdentificationContainer;
import com.wynntils.models.gear.profile.GearProfile;
import com.wynntils.models.gear.profile.IdentificationProfile;
import com.wynntils.models.gear.profile.MajorIdentification;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.IdentificationModifier;
import com.wynntils.models.gear.type.RequirementType;
import com.wynntils.models.gearinfo.GearInfo;
import com.wynntils.models.gearinfo.GearStatsFixed;
import com.wynntils.models.gearinfo.types.GearDamageType;
import com.wynntils.models.items.items.game.GearItem;
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
        GearStatsFixed fixedStats = gearInfo.statsFixed();
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

        String idName = identificationMatcher.group("ID");
        boolean isRaw = identificationMatcher.group("Suffix") == null;

        String shortIdName = WynnItemMatchers.getShortIdentificationName(idName, isRaw);

        IdentificationProfile idProfile = item.getStatuses().get(shortIdName);
        if (idProfile != null) {
            return idProfile.getType() != null;
        } else {
            return IdentificationProfile.getTypeFromName(shortIdName) != null;
        }
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
        List<Component> tooltips = middleTooltipCache.get(style);
        if (tooltips != null) return tooltips;

        tooltips = buildMiddleTooltip(style);
        middleTooltipCache.put(style, tooltips);
        return tooltips;
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

        Map<String, Component> map =
                switch (style.decorations()) {
                    case PERCENT -> idContainers.stream()
                            .collect(Collectors.toMap(
                                    GearIdentificationContainer::shortIdName, this::buildPercentLoreLine));
                    case RANGE -> idContainers.stream()
                            .collect(Collectors.toMap(
                                    GearIdentificationContainer::shortIdName, this::buildRangeLoreLine));
                    case REROLL_CHANCE -> idContainers.stream()
                            .collect(Collectors.toMap(
                                    GearIdentificationContainer::shortIdName, this::buildRerollLoreLine));
                };

        if (style.reorder()) {
            return Models.GearProfiles.orderComponents(map, style.group());
        } else {
            return new ArrayList<>(map.values());
        }
    }

    private Component buildRerollLoreLine(GearIdentificationContainer idContainer) {
        MutableComponent rerollLine = buildBaseComponent(idContainer);
        IdentificationProfile idProfile = idContainer.idProfile();
        if (!idProfile.hasConstantValue()) {
            ReidentificationChances chances = getChances(idProfile, idContainer.value(), idContainer.stars());

            rerollLine.append(
                    getRerollChancesComponent(idProfile.getPerfectChance(), chances.increase(), chances.decrease()));
        }

        return rerollLine;
    }

    public ReidentificationChances getChances(IdentificationProfile idProfile, int currentValue, int starCount) {
        boolean isInverted = idProfile.isInverted();
        int baseValue = idProfile.getBaseValue();
        // Accounts for bounds - api isn't updated. Furthermore, there does exist the fact
        // that some items that have had its stats shifted from positive to negative to
        // break the bounds
        if (currentValue > idProfile.getMax()) {
            return new ReidentificationChances(1d, 0d, 0d).flipIf(isInverted);
        } else if (currentValue < idProfile.getMin()) {
            return new ReidentificationChances(0d, 0d, 1d).flipIf(isInverted);
        }

        if (idProfile.hasConstantValue()) {
            return new ReidentificationChances(0d, 1d, 0d).flipIf(isInverted);
        }

        // This code finds the lowest possible and highest possible rolls that achieve the correct
        // result (inclusive). Then, it finds the average decrease and increase afterwards

        // Note that due to rounding, a bound may not actually be a possible roll
        // if it results in a value that is exactly .5, which then rounds up/down

        double lowerRawRollBound = (currentValue * 100 - 50) / ((double) baseValue);
        double higherRawRollBound = (currentValue * 100 + 50) / ((double) baseValue);

        if (baseValue > 0) {
            // We can further bound the possible rolls using the star count
            int starMin = 30;
            int starMax = 130;

            switch (starCount) {
                case 0:
                    starMin = 30;
                    starMax = 100;
                    break;
                case 1:
                    starMin = 101;
                    starMax = 124;
                    break;
                case 2:
                    starMin = 125;
                    starMax = 129;
                    break;
                case 3:
                    return new ReidentificationChances(100 / 101d, 1 / 101d, 0d);
                default:
                    WynntilsMod.warn("Invalid star count of " + starCount);
            }

            double lowerRollBound = Math.max(Math.ceil(lowerRawRollBound), starMin);
            double higherRollBound = Math.min(Math.ceil(higherRawRollBound) - 1, starMax);

            double avg = (lowerRollBound + higherRollBound) / 2d;

            return new ReidentificationChances((avg - 30) / 101d, 1 / 101d, (130 - avg) / 101d).flipIf(isInverted);
        } else {
            double lowerRollBound = Math.min(Math.ceil(lowerRawRollBound) - 1, 130);
            double higherRollBound = Math.max(Math.ceil(higherRawRollBound), 70);

            double avg = (lowerRollBound + higherRollBound) / 2d;

            return new ReidentificationChances((130 - avg) / 61d, 1 / 61d, (avg - 70) / 61d).flipIf(isInverted);
        }
    }

    private static MutableComponent getRerollChancesComponent(double perfect, double increase, double decrease) {
        return Component.literal(String.format(Locale.ROOT, " \u2605%.2f%%", perfect * 100))
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format(Locale.ROOT, " \u21E7%.1f%%", increase * 100))
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(String.format(Locale.ROOT, " \u21E9%.1f%%", decrease * 100))
                        .withStyle(ChatFormatting.RED));
    }

    private Component buildRangeLoreLine(GearIdentificationContainer idContainer) {
        MutableComponent rangeLine = buildBaseComponent(idContainer);
        IdentificationProfile idProfile = idContainer.idProfile();
        if (!idProfile.hasConstantValue()) {
            // calculate percent/range/reroll chances, append to lines
            int min = idProfile.getMin();
            int max = idProfile.getMax();

            rangeLine.append(getRangeTextComponent(min, max));
        }

        return rangeLine;
    }

    private static MutableComponent getRangeTextComponent(int min, int max) {
        return Component.literal(" [")
                .append(Component.literal(min + ", " + max).withStyle(ChatFormatting.GREEN))
                .append("]")
                .withStyle(ChatFormatting.DARK_GREEN);
    }

    private Component buildPercentLoreLine(GearIdentificationContainer idContainer) {
        MutableComponent percentLine = buildBaseComponent(idContainer);
        IdentificationProfile idProfile = idContainer.idProfile();
        if (!idProfile.hasConstantValue()) {
            // calculate percent/range/reroll chances, append to lines
            int min = idProfile.getMin();
            int max = idProfile.getMax();

            float percentage = MathUtils.inverseLerp(min, max, idContainer.value()) * 100;
            percentLine.append(getPercentageTextComponent(percentage));
        }
        return percentLine;
    }

    private static MutableComponent getPercentageTextComponent(float percentage) {
        return ColorScaleUtils.getPercentageTextComponent(
                percentage, ItemStatInfoFeature.INSTANCE.colorLerp, ItemStatInfoFeature.INSTANCE.decimalPlaces);
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

        MutableComponent baseComponent = getBaseComponent(
                idContainer.inGameIdName(), idContainer.value(), idContainer.stars(), isInverted, unit);

        return baseComponent;
    }

    private static MutableComponent getBaseComponent(
            String idName, int value, int starCount, boolean isInverted, String unit) {
        MutableComponent baseComponent = Component.literal("");

        MutableComponent statInfo = Component.literal((value > 0 ? "+" : "") + value + unit);
        statInfo.setStyle(Style.EMPTY.withColor(isInverted ^ (value > 0) ? ChatFormatting.GREEN : ChatFormatting.RED));

        baseComponent.append(statInfo);

        if (ItemStatInfoFeature.INSTANCE.showStars)
            baseComponent.append(
                    Component.literal("***".substring(3 - starCount)).withStyle(ChatFormatting.DARK_GREEN));

        baseComponent.append(Component.literal(" " + idName).withStyle(ChatFormatting.GRAY));
        return baseComponent;
    }

    public record ReidentificationChances(double decrease, double remain, double increase) {

        private ReidentificationChances flipIf(boolean flip) {
            if (flip) return new ReidentificationChances(increase, remain, decrease);

            return this;
        }
    }

    public enum IdentificationDecorations {
        PERCENT,
        RANGE,
        REROLL_CHANCE
    }

    public record IdentificationPresentationStyle(
            IdentificationDecorations decorations, boolean reorder, boolean group) {}
}
