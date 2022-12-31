/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.core.components.Managers;
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.handleditems.items.game.GearItem;
import com.wynntils.wynn.objects.ItemIdentificationContainer;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.profiles.item.DamageType;
import com.wynntils.wynn.objects.profiles.item.GearIdentification;
import com.wynntils.wynn.objects.profiles.item.IdentificationProfile;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.MajorIdentification;
import com.wynntils.wynn.objects.profiles.item.RequirementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;

public class GearTooltipBuilder {
    private static final Component ID_PLACEHOLDER = Component.literal("ID_PLACEHOLDER");

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
        buildTooltips();
    }

    private void buildTooltips() {
        topTooltip = getTooltipTop();
        bottomTooltip = getTooltipBottom();

        constructTooltips();
    }

    private List<Component> getTooltipTop() {
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

    private List<Component> getTooltipBottom() {
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

    private void constructTooltips() {
        percentTooltip = new ArrayList<>();
        rangeTooltip = new ArrayList<>();
        rerollTooltip = new ArrayList<>();

        List<Component> orderedPercents;
        List<Component> orderedRanges;
        List<Component> orderedRerolls;

        Map<String, Component> percentMap;
        Map<String, Component> rangeMap;
        Map<String, Component> rerollMap;

        if (gearItem == null) {
            Collection<IdentificationProfile> ids = itemProfile.getStatuses().values();

            // FIXME: broken
            List<ItemIdentificationContainer> identifications = WynnItemUtils.identificationsFromProfile(itemProfile);
            percentMap =
                    ids.stream().collect(Collectors.toMap(id -> id.toString(), id -> Component.literal(id.toString())));
            rangeMap =
                    ids.stream().collect(Collectors.toMap(id -> id.toString(), id -> Component.literal(id.toString())));
            rerollMap =
                    ids.stream().collect(Collectors.toMap(id -> id.toString(), id -> Component.literal(id.toString())));
        } else {
            // ItemIdentificationContainer
            List<GearIdentification> identifications = gearItem.getIdentifications();

            // FIXME: BROKEN
            percentMap = identifications.stream()
                    .collect(Collectors.toMap(id -> id.getIdName(), id -> Component.literal(id.getIdName())));
            rangeMap = identifications.stream()
                    .collect(Collectors.toMap(id -> id.getIdName(), id -> Component.literal(id.getIdName())));
            rerollMap = identifications.stream()
                    .collect(Collectors.toMap(id -> id.getIdName(), id -> Component.literal(id.getIdName())));
        }

        orderedPercents =
                Managers.ItemProfiles.orderComponents(percentMap, ItemStatInfoFeature.INSTANCE.groupIdentifications);
        orderedRanges =
                Managers.ItemProfiles.orderComponents(rangeMap, ItemStatInfoFeature.INSTANCE.groupIdentifications);
        orderedRerolls =
                Managers.ItemProfiles.orderComponents(rerollMap, ItemStatInfoFeature.INSTANCE.groupIdentifications);

        percentTooltip = orderedPercents;
        rangeTooltip = orderedRanges;
        rerollTooltip = orderedRerolls;
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
