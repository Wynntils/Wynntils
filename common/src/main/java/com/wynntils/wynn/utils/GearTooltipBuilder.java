/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.core.components.Managers;
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.utils.Pair;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.handleditems.items.game.GearItem;
import com.wynntils.wynn.objects.ItemIdentificationContainer;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.profiles.item.DamageType;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.MajorIdentification;
import com.wynntils.wynn.objects.profiles.item.RequirementType;
import java.util.ArrayList;
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

    private final ItemProfile itemProfile;
    private final GearItem gearItem;

    private List<Component> topTooltip;
    private List<Component> bottomTooltip;

    private List<Component> percentTooltip;
    private List<Component> rangeTooltip;
    private List<Component> rerollTooltip;

    private GearTooltipBuilder(ItemProfile itemProfile, GearItem gearItem) {
        this.itemProfile = itemProfile;
        this.gearItem = gearItem;

        topTooltip = buildTopTooltip();
        bottomTooltip = buildBottomTooltip();

        constructMiddleTooltips();
    }

    private GearTooltipBuilder(
            ItemProfile itemProfile, GearItem gearItem, List<Component> topTooltip, List<Component> bottomTooltip) {
        this.itemProfile = itemProfile;
        this.gearItem = gearItem;

        this.topTooltip = topTooltip;
        this.bottomTooltip = bottomTooltip;

        constructMiddleTooltips();
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

    public List<Component> getTooltipLines(IdentificationDecorations decorations) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());

        // Top and bottom are always constant
        tooltip.addAll(topTooltip);

        // In the middle we have the list of identifications, which is different
        // depending on which decorations are requested
        tooltip.addAll(getMiddlePart(decorations));

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

            ItemIdentificationContainer idContainer =
                    Managers.ItemProfiles.identificationFromLore(loreLine, gearProfile);
            if (idContainer == null) { // not an ID line
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

    private void constructMiddleTooltips() {
        List<ItemIdentificationContainer> identifications = gearItem.getIdContainers();

        if (identifications.isEmpty()) {
            percentTooltip = List.of();
            rangeTooltip = List.of();
            rerollTooltip = List.of();
            return;
        }

        Map<String, Component> percentMap = identifications.stream()
                .collect(Collectors.toMap(
                        ItemIdentificationContainer::shortIdName, ItemIdentificationContainer::percentLoreLine));
        Map<String, Component> rangeMap = identifications.stream()
                .collect(Collectors.toMap(
                        ItemIdentificationContainer::shortIdName, ItemIdentificationContainer::rangeLoreLine));
        Map<String, Component> rerollMap = identifications.stream()
                .collect(Collectors.toMap(
                        ItemIdentificationContainer::shortIdName, ItemIdentificationContainer::rerollLoreLine));

        if (ItemStatInfoFeature.INSTANCE.reorderIdentifications) {
            percentTooltip = Managers.ItemProfiles.orderComponents(
                    percentMap, ItemStatInfoFeature.INSTANCE.groupIdentifications);
            rangeTooltip =
                    Managers.ItemProfiles.orderComponents(rangeMap, ItemStatInfoFeature.INSTANCE.groupIdentifications);
            rerollTooltip =
                    Managers.ItemProfiles.orderComponents(rerollMap, ItemStatInfoFeature.INSTANCE.groupIdentifications);
        } else {
            percentTooltip = new ArrayList<>(percentMap.values());
            rangeTooltip = new ArrayList<>(rangeMap.values());
            rerollTooltip = new ArrayList<>(rerollMap.values());
        }
    }

    private Component getHoverName() {
        return Component.literal(gearItem.getItemProfile().getDisplayName())
                .withStyle(gearItem.getGearTier().getChatFormatting());
    }

    private List<Component> getMiddlePart(IdentificationDecorations decorations) {
        if (gearItem == null) {
            return rangeTooltip;
        } else {
            return switch (decorations) {
                case PERCENT -> percentTooltip;
                case RANGE -> rangeTooltip;
                case REROLL_CHANCE -> rerollTooltip;
            };
        }
    }

    public enum IdentificationDecorations {
        PERCENT,
        RANGE,
        REROLL_CHANCE
    }
}
