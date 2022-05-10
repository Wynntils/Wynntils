/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.item;

import com.mojang.blaze3d.vertex.*;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.item.ItemProfile;
import com.wynntils.features.ItemStatInfoFeature;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.wc.objects.item.render.HighlightedItem;
import com.wynntils.wc.objects.item.render.HotbarHighlightedItem;
import com.wynntils.wc.utils.IdentificationOrderer;
import com.wynntils.wc.utils.WynnUtils;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.lwjgl.glfw.GLFW;

public class WynnGearStack extends WynnItemStack implements HighlightedItem, HotbarHighlightedItem {

    private ItemProfile itemProfile;
    private boolean isPerfect;
    private boolean isDefective;
    private float overallPercentage;

    Component percentName;

    List<ItemIdentificationContainer> identifications;

    List<Component> percentTooltip;
    List<Component> rangeTooltip;
    List<Component> rerollTooltip;

    public WynnGearStack(ItemStack stack) {
        super(stack);

        // get item profile
        if (WebManager.getItemsMap() == null || !WebManager.getItemsMap().containsKey(itemName)) return;
        itemProfile = WebManager.getItemsMap().get(itemName);

        // identification parsing & tooltip creation
        identifications = new ArrayList<>();

        List<Component> lore = stripDuplicateBlank(super.getTooltipLines(null, TooltipFlag.Default.NORMAL));
        lore.remove(0); // remove item name

        List<Component> baseTooltip = new ArrayList<>();

        int idAmount = 0; // only counting non-fixed IDs known by the API
        float percentTotal = 0;
        boolean hasNew = false;

        int idStart = -1;
        boolean endOfIDs = false;
        for (int i = 0; i < lore.size(); i++) {
            Component loreLine = lore.get(i);
            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLine.getString());

            if (unformattedLoreLine.equals("Set Bonus:")) {
                endOfIDs = true;
            }

            if (endOfIDs) continue;

            ItemIdentificationContainer idContainer = ItemIdentificationContainer.fromLore(loreLine, itemProfile);
            if (idContainer == null) { // not an ID line
                baseTooltip.add(loreLine);
                continue;
            }

            // if we've reached this point, we have an id
            if (idStart == -1) {
                idStart = i;
            }

            identifications.add(idContainer);

            if (!idContainer.isNew() && !idContainer.isFixed()) {
                idAmount++;
                percentTotal += idContainer.getPercent();
            }

            if (idContainer.isNew()) {
                hasNew = true;
            }
        }

        if (!identifications.isEmpty()) {
            baseTooltip = stripDuplicateBlank(baseTooltip);
            baseTooltip.add(idStart - 1, new TextComponent(""));
        }

        percentTooltip = new ArrayList<>(baseTooltip);
        rangeTooltip = new ArrayList<>(baseTooltip);
        rerollTooltip = new ArrayList<>(baseTooltip);

        if (!identifications.isEmpty()) {
            Map<String, Component> percentMap = identifications.stream()
                    .collect(Collectors.toMap(
                            ItemIdentificationContainer::getShortIdName,
                            ItemIdentificationContainer::getPercentLoreLine));
            Map<String, Component> rangeMap = identifications.stream()
                    .collect(Collectors.toMap(
                            ItemIdentificationContainer::getShortIdName,
                            ItemIdentificationContainer::getRangeLoreLine));
            Map<String, Component> rerollMap = identifications.stream()
                    .collect(Collectors.toMap(
                            ItemIdentificationContainer::getShortIdName,
                            ItemIdentificationContainer::getRerollLoreLine));

            Collection<Component> orderedPercents;
            Collection<Component> orderedRanges;
            Collection<Component> orderedRerolls;

            if (ItemStatInfoFeature.reorderIdentifications) {
                orderedPercents = IdentificationOrderer.INSTANCE.orderComponents(
                        percentMap, ItemStatInfoFeature.groupIdentifications);
                orderedRanges = IdentificationOrderer.INSTANCE.orderComponents(
                        rangeMap, ItemStatInfoFeature.groupIdentifications);
                orderedRerolls = IdentificationOrderer.INSTANCE.orderComponents(
                        rerollMap, ItemStatInfoFeature.groupIdentifications);
            } else {
                orderedPercents = percentMap.values();
                orderedRanges = rangeMap.values();
                orderedRerolls = rerollMap.values();
            }

            percentTooltip.addAll(idStart, orderedPercents);
            rangeTooltip.addAll(idStart, orderedRanges);
            rerollTooltip.addAll(idStart, orderedRerolls);
        }

        // overall percent & name
        overallPercentage = -1f;

        String originalName = WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(getHoverName()));
        MutableComponent name = new TextComponent(originalName);

        if (hasNew) {
            name.append(new TextComponent(" [NEW]").withStyle(ChatFormatting.GOLD));
        } else if (idAmount > 0) {
            overallPercentage = percentTotal / idAmount;

            // check for perfect/0% items
            isPerfect = overallPercentage >= 100d;
            isDefective = overallPercentage == 0;

            name.append(ItemStatInfoFeature.getPercentageTextComponent(overallPercentage));
        }

        percentName = name;
    }

    public WynnGearStack(ItemProfile profile) {
        super(profile.getItemInfo().asItemStack());
        // TODO guide stack impl
    }

    public ItemProfile getItemProfile() {
        return itemProfile;
    }

    public float getOverallPercentage() {
        return overallPercentage;
    }

    @Override
    public Component getHoverName() {
        /*
         * This math was originally based off Avaritia code.
         * Special thanks for Morpheus1101 and SpitefulFox
         * Avaritia Repo: https://github.com/Morpheus1101/Avaritia
         */
        if (ItemStatInfoFeature.perfect && isPerfect) {
            MutableComponent newName = new TextComponent("").withStyle(ChatFormatting.BOLD);

            String name = "Perfect " + itemName;

            long time = System.currentTimeMillis();
            for (int i = 0; i < name.length(); i++) {
                int cycle = 1000;
                Style color = Style.EMPTY
                        .withColor(Color.HSBtoRGB(((time + i * cycle / 7) % cycle) / (float) cycle, 0.8F, 0.8F))
                        .withItalic(false);

                newName.append(new TextComponent(String.valueOf(name.charAt(i))).setStyle(color));
            }

            return newName;
        }

        if (ItemStatInfoFeature.defective && isDefective) {
            MutableComponent newName = new TextComponent("").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);
            newName.setStyle(newName.getStyle().withItalic(false));

            String name = "Defective " + itemName;

            boolean obfuscated = Math.random() < ItemStatInfoFeature.obfuscationChanceStart;
            StringBuilder current = new StringBuilder();

            for (int i = 0; i < name.length() - 1; i++) {
                current.append(name.charAt(i));

                float chance = MathUtils.lerp(
                        ItemStatInfoFeature.obfuscationChanceStart,
                        ItemStatInfoFeature.obfuscationChanceEnd,
                        (i + 1) / (float) (name.length() - 1));

                if (!obfuscated && Math.random() < chance) {
                    newName.append(new TextComponent(current.toString()).withStyle(Style.EMPTY.withItalic(false)));
                    current = new StringBuilder();

                    obfuscated = true;
                } else if (obfuscated && Math.random() > chance) {
                    newName.append(new TextComponent(current.toString())
                            .withStyle(Style.EMPTY.withObfuscated(true).withItalic(false)));
                    current = new StringBuilder();

                    obfuscated = false;
                }
            }

            current.append(name.charAt(name.length() - 1));

            if (obfuscated) {
                newName.append(new TextComponent(current.toString())
                        .withStyle(Style.EMPTY.withItalic(false).withObfuscated(true)));
            } else {
                newName.append(new TextComponent(current.toString()).withStyle(Style.EMPTY.withItalic(false)));
            }

            return newName;
        }

        // besides special case of perfect/defective, use name already set
        return percentName != null ? percentName : super.getHoverName();
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());

        if (GLFW.glfwGetKey(McUtils.mc().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == 1) {
            tooltip.addAll(rangeTooltip);
        } else if (GLFW.glfwGetKey(McUtils.mc().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) == 1) {
            tooltip.addAll(rerollTooltip);
        } else {
            tooltip.addAll(percentTooltip);
        }

        return tooltip;
    }

    @Override
    public int getHighlightColor(SlotRenderEvent e) {
        int color = itemProfile.getTier().getChatFormatting().getColor();
        color = 0xFF000000 | color;

        return color;
    }

    @Override
    public int getHotbarColor(HotbarSlotRenderEvent e) {
        int color = itemProfile.getTier().getChatFormatting().getColor();
        color = 0x80000000 | color;

        return color;
    }

    private static List<Component> stripDuplicateBlank(List<Component> lore) {
        List<Component> newLore = new ArrayList<>(); // Used to remove duplicate blank lines

        boolean oldBlank = false;
        int index = 0;

        for (; index < lore.size(); index++) { // find first blank
            Component loreLine = lore.get(index);

            String line = WynnUtils.normalizeBadString(loreLine.getString());

            newLore.add(loreLine);

            if (line.isEmpty()) {
                oldBlank = true;
                break;
            }
        }

        if (!oldBlank) {
            return newLore;
        }

        for (; index < lore.size(); index++) {
            Component loreLine = lore.get(index);

            String line = WynnUtils.normalizeBadString(loreLine.getString());

            if (oldBlank && line.isEmpty()) {
                continue; // both blank - do not add; oldBlank still true
            }

            oldBlank = line.isEmpty();

            newLore.add(loreLine);
        }

        return newLore;
    }
}
