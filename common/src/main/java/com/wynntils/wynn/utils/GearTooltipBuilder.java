/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.core.components.Managers;
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.utils.KeyboardUtils;
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
import org.lwjgl.glfw.GLFW;

public class GearTooltipBuilder {
    private static final Pattern ITEM_TIER =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) Item(?: \\[(?<Rolls>\\d+)])?");

    private final ItemProfile itemProfile;
    private final GearItem gearItem;

    private List<Component> topTooltip;
    private List<Component> bottomTooltip;

    private List<Component> percentTooltip;
    private List<Component> rangeTooltip;
    private List<Component> rerollTooltip;
    private boolean isChatItem;

    public GearTooltipBuilder(ItemProfile gearProfile) {
        this(gearProfile, null, false);
    }

    public GearTooltipBuilder(GearItem gearItem) {
        this(gearItem.getItemProfile(), gearItem, false);
    }

    public GearTooltipBuilder(ItemProfile gearProfile, GearItem gearItem, boolean isChatItem) {
        this.itemProfile = gearProfile;
        this.gearItem = gearItem;
        this.isChatItem = isChatItem;

        topTooltip = buildTooltipTop();
        bottomTooltip = buildTooltipBottom();

        constructMiddleTooltips();
    }

    public GearTooltipBuilder(
            ItemProfile gearProfile,
            GearItem gearItem,
            boolean isChatItem,
            List<Component> topTooltip,
            List<Component> bottomTooltip) {
        this.itemProfile = gearProfile;
        this.gearItem = gearItem;
        this.isChatItem = isChatItem;

        this.topTooltip = topTooltip;
        this.bottomTooltip = bottomTooltip;

        constructMiddleTooltips();
    }

    public static GearTooltipBuilder fromItemStack(
            ItemStack itemStack, ItemProfile gearProfile, GearItem gearItem, boolean isChatItem) {
        List<Component> tooltips = itemStack.getTooltipLines(null, TooltipFlag.NORMAL);

        // Skip first line which contains name
        Pair<List<Component>, List<Component>> splittedLore =
                splitLore(tooltips.subList(1, tooltips.size()), gearProfile);

        return new GearTooltipBuilder(gearProfile, gearItem, isChatItem, splittedLore.a(), splittedLore.b());
    }

    static Pair<List<Component>, List<Component>> splitLore(List<Component> lore, ItemProfile gearProfile) {
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

    private static List<Component> extractTooltipBottom(List<Component> tooltips) {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal("BOTTOM"));
        return list;
    }

    private List<Component> buildTooltipTop() {
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
            // dps
            MutableComponent dpsLine = Component.literal("   Average DPS: ").withStyle(ChatFormatting.DARK_GRAY);
            // FIXME: calculate DPS
            dpsLine.append(Component.literal("???").withStyle(ChatFormatting.GRAY));
            baseTooltip.add(dpsLine);

            baseTooltip.add(Component.literal(""));
        }

        // elemental defenses
        if (!itemProfile.getDefenseTypes().isEmpty()) {
            int health = itemProfile.getHealth();
            if (health != 0) {
                MutableComponent healthComp =
                        Component.literal("❤ Health: " + withSign(health)).withStyle(ChatFormatting.DARK_RED);
                baseTooltip.add(healthComp);
            }

            Map<DamageType, Integer> defenses = itemProfile.getElementalDefenses();
            for (Map.Entry<DamageType, Integer> entry : defenses.entrySet()) {
                DamageType type = entry.getKey();
                MutableComponent defense =
                        Component.literal(type.getSymbol() + " " + type).withStyle(type.getColor());
                defense.append(Component.literal(" Defence: " + withSign(entry.getValue()))
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

                if (Managers.Character.isRequirementSatisfied(type, entry.getValue())) {
                    requirement = Component.literal("✔ ").withStyle(ChatFormatting.GREEN);
                } else {
                    requirement = Component.literal("✖ ").withStyle(ChatFormatting.RED);
                }
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

    private String withSign(int value) {
        if (value >= 0) {
            return "+" + value;
        } else {
            return Integer.toString(value);
        }
    }

    private List<Component> buildTooltipBottom() {
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

    public List<Component> getTooltipLines() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());

        tooltip.addAll(topTooltip);

        // Get middle part
        tooltip.addAll(getMiddlePart());

        tooltip.addAll(bottomTooltip);

        return tooltip;
    }

    private Component getHoverName() {
        return Component.literal(gearItem.getItemProfile().getDisplayName())
                .withStyle(gearItem.getGearTier().getChatFormatting());
    }

    public List<Component> getMiddlePart() {

        if (gearItem == null) {
            return rangeTooltip;
        } else if (isChatItem) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("From chat")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .withStyle(ChatFormatting.ITALIC));

            tooltip.addAll(percentTooltip);
            return tooltip;
        } else {
            if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                return rangeTooltip;
            } else if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                return rerollTooltip;
            } else {
                return percentTooltip;
            }
        }
    }
}
