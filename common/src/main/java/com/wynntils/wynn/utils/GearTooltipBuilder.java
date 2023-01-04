/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.core.components.Managers;
import com.wynntils.utils.Pair;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.handleditems.items.game.GearItem;
import com.wynntils.wynn.objects.ItemIdentificationContainer;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.SpellType;
import com.wynntils.wynn.objects.profiles.item.DamageType;
import com.wynntils.wynn.objects.profiles.item.IdentificationProfile;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.MajorIdentification;
import com.wynntils.wynn.objects.profiles.item.RequirementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class GearTooltipBuilder {
    private static final Pattern ITEM_TIER =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) Item(?: \\[(?<Rolls>\\d+)])?");
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    private final ItemProfile itemProfile;
    private final GearItem gearItem;

    private List<Component> topTooltip;
    private List<Component> bottomTooltip;

    private Map<IdentificationPresentationStyle, List<Component>> middleTooltipCache = new HashMap<>();

    private GearTooltipBuilder(ItemProfile itemProfile, GearItem gearItem) {
        this.itemProfile = itemProfile;
        this.gearItem = gearItem;

        topTooltip = buildTopTooltip();
        bottomTooltip = buildBottomTooltip();
    }

    private GearTooltipBuilder(
            ItemProfile itemProfile, GearItem gearItem, List<Component> topTooltip, List<Component> bottomTooltip) {
        this.itemProfile = itemProfile;
        this.gearItem = gearItem;

        this.topTooltip = topTooltip;
        this.bottomTooltip = bottomTooltip;
    }

    public static GearTooltipBuilder fromItemProfile(ItemProfile itemProfile) {
        return new GearTooltipBuilder(itemProfile, null);
    }

    public static GearTooltipBuilder fromGearItem(GearItem gearItem) {
        return new GearTooltipBuilder(gearItem.getItemProfile(), gearItem);
    }

    public static GearTooltipBuilder fromItemStack(ItemStack itemStack, ItemProfile itemProfile, GearItem gearItem) {
        List<Component> tooltips = itemStack.getTooltipLines(null, TooltipFlag.NORMAL);

        // Skip first line which contains name
        Pair<List<Component>, List<Component>> splittedLore =
                splitLore(tooltips.subList(1, tooltips.size()), itemProfile);

        return new GearTooltipBuilder(itemProfile, gearItem, splittedLore.a(), splittedLore.b());
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

        return tooltip;
    }

    private static Pair<List<Component>, List<Component>> splitLore(List<Component> lore, ItemProfile gearProfile) {
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

    public static boolean isIdLine(Component lore, ItemProfile item) {
        // This looks quite messy, but is in effect what we did before
        // FIXME: Clean up?
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());
        Matcher identificationMatcher = ITEM_IDENTIFICATION_PATTERN.matcher(unformattedLoreLine);
        if (!identificationMatcher.find()) return false; // not a valid id line

        String idName = identificationMatcher.group("ID");
        boolean isRaw = identificationMatcher.group("Suffix") == null;

        String shortIdName;
        SpellType spell = SpellType.fromName(idName);
        if (spell != null) {
            shortIdName = spell.getShortIdName(isRaw);
        } else {
            shortIdName = IdentificationProfile.getAsShortName(idName, isRaw);
        }

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
        if (itemProfile.getAttackSpeed() != null)
            baseTooltip.add(Component.literal(itemProfile.getAttackSpeed().asLore()));

        baseTooltip.add(Component.literal(""));

        // elemental damages
        if (!itemProfile.getDamageTypes().isEmpty()) {
            Map<DamageType, String> damages = itemProfile.getDamages();
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
        if (!itemProfile.getDefenseTypes().isEmpty()) {
            int health = itemProfile.getHealth();
            if (health != 0) {
                MutableComponent healthComp = Component.literal("❤ Health: " + StringUtils.toSignedString(health))
                        .withStyle(ChatFormatting.DARK_RED);
                baseTooltip.add(healthComp);
            }

            Map<DamageType, Integer> defenses = itemProfile.getElementalDefenses();
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
        if (itemProfile.hasRequirements()) {
            Map<RequirementType, String> requirements = itemProfile.getRequirements();
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
        if (!itemProfile.getStatuses().isEmpty()) {
            baseTooltip.add(Component.literal(""));
        }

        return baseTooltip;
    }

    private List<Component> buildBottomTooltip() {
        List<Component> baseTooltip = new ArrayList<>();

        // major ids
        if (itemProfile.getMajorIds() != null && !itemProfile.getMajorIds().isEmpty()) {
            for (MajorIdentification majorId : itemProfile.getMajorIds()) {
                Stream.of(StringUtils.wrapTextBySize(majorId.asLore(), 150))
                        .forEach(c -> baseTooltip.add(Component.literal(c).withStyle(ChatFormatting.DARK_AQUA)));
            }
            baseTooltip.add(Component.literal(""));
        }

        // powder slots
        if (itemProfile.getPowderAmount() > 0) {
            if (gearItem == null) {
                baseTooltip.add(Component.literal("[" + itemProfile.getPowderAmount() + " Powder Slots]")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                MutableComponent powderLine = Component.literal(
                                "[" + gearItem.getPowders().size() + "/" + itemProfile.getPowderAmount()
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
        MutableComponent tier = itemProfile.getTier().asLore().copy();
        if (gearItem != null && gearItem.getRerolls() > 1) {
            tier.append(" [" + gearItem.getRerolls() + "]");
        }
        baseTooltip.add(tier);

        // untradable
        if (itemProfile.getRestriction() != null) {
            baseTooltip.add(Component.literal(StringUtils.capitalizeFirst(itemProfile.getRestriction() + " Item"))
                    .withStyle(ChatFormatting.RED));
        }

        String lore = itemProfile.getLore();
        if (lore != null) {
            Stream.of(StringUtils.wrapTextBySize(lore, 150))
                    .forEach(c -> baseTooltip.add(Component.literal(c).withStyle(ChatFormatting.DARK_GRAY)));
        }

        return baseTooltip;
    }

    private Component getHoverName() {
        return Component.literal(itemProfile.getDisplayName())
                .withStyle(itemProfile.getTier().getChatFormatting());
    }

    private List<Component> getMiddleTooltip(IdentificationPresentationStyle style) {
        List<Component> tooltips = middleTooltipCache.get(style);
        if (tooltips != null) return tooltips;

        tooltips = buildMiddleTooltip(style);
        middleTooltipCache.put(style, tooltips);
        return tooltips;
    }

    private List<Component> buildMiddleTooltip(IdentificationPresentationStyle style) {
        List<ItemIdentificationContainer> idContainers;
        if (gearItem == null) {
            idContainers = WynnItemUtils.identificationsFromProfile(itemProfile);
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
                                    ItemIdentificationContainer::shortIdName,
                                    ItemIdentificationContainer::percentLoreLine));
                    case RANGE -> idContainers.stream()
                            .collect(Collectors.toMap(
                                    ItemIdentificationContainer::shortIdName,
                                    ItemIdentificationContainer::rangeLoreLine));
                    case REROLL_CHANCE -> idContainers.stream()
                            .collect(Collectors.toMap(
                                    ItemIdentificationContainer::shortIdName,
                                    ItemIdentificationContainer::rerollLoreLine));
                };

        if (style.reorder()) {
            return Managers.ItemProfiles.orderComponents(map, style.group());
        } else {
            return new ArrayList<>(map.values());
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
