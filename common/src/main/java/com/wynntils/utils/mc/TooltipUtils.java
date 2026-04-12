/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearTooltipAlignmentComponent;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.utils.render.FontRenderer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public final class TooltipUtils {
    public static int getTooltipWidth(List<ClientTooltipComponent> lines, Font font) {
        return lines.stream()
                .map(clientTooltipComponent -> clientTooltipComponent.getWidth(font))
                .max(Integer::compareTo)
                .orElse(0);
    }

    public static int getTooltipHeight(List<ClientTooltipComponent> lines) {
        return (lines.size() == 1 ? -2 : 0)
                + lines.stream()
                        .mapToInt(clientTooltip -> clientTooltip.getHeight(
                                FontRenderer.getInstance().getFont()))
                        .sum();
    }

    public static List<ClientTooltipComponent> getClientTooltipComponent(List<Component> components) {
        return components.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }

    public static List<Component> getWynnItemTooltip(ItemStack itemStack, WynnItem wynnItem) {
        List<Component> tooltip = new ArrayList<>();

        Optional<IdentifiableItemProperty> identifiableItemPropertyOpt =
                Models.Item.asWynnItemProperty(itemStack, IdentifiableItemProperty.class);
        if (identifiableItemPropertyOpt.isPresent()) {
            tooltip = getIdentifiableItemTooltip(itemStack, wynnItem, identifiableItemPropertyOpt.get());
        }

        Optional<CraftedItemProperty> craftedItemPropertyOpt =
                Models.Item.asWynnItemProperty(itemStack, CraftedItemProperty.class);
        if (craftedItemPropertyOpt.isPresent()) {
            tooltip = getCraftedItemTooltip(itemStack, wynnItem, craftedItemPropertyOpt.get());
        }

        return tooltip;
    }

    public static void realignMarkedTooltipLines(List<Component> tooltips) {
        GearTooltipAlignmentComponent.realignMarkedTooltipLines(tooltips);
    }

    public static boolean containsFont(Component component, FontDescription font) {
        if (font.equals(component.getStyle().getFont())) {
            return true;
        }

        for (Component sibling : component.getSiblings()) {
            if (containsFont(sibling, font)) {
                return true;
            }
        }

        return false;
    }

    public static int findFirstLineWithFont(List<Component> tooltips, FontDescription font) {
        for (int i = 0; i < tooltips.size(); i++) {
            if (containsFont(tooltips.get(i), font)) {
                return i;
            }
        }

        return -1;
    }

    public static int findFirstNonBlankLine(List<Component> tooltips) {
        for (int i = 0; i < tooltips.size(); i++) {
            if (!tooltips.get(i).getString().isBlank()) {
                return i;
            }
        }

        return -1;
    }

    public static boolean replaceTrailingTitleComponent(
            MutableComponent line, String itemName, MutableComponent replacement) {
        List<Component> siblings = line.getSiblings();
        for (int i = siblings.size() - 1; i >= 0; i--) {
            String siblingText = siblings.get(i).getString().trim();
            if (!siblingText.equals(itemName) && !siblingText.endsWith(itemName)) {
                continue;
            }

            siblings.set(i, replacement);
            return true;
        }

        return false;
    }

    public static Component extractTrailingSegmentWithFont(Component component, FontDescription font) {
        List<Component> siblings = component.getSiblings();
        if (siblings.isEmpty()) {
            return containsFont(component, font) ? component.copy() : null;
        }

        int startIndex = -1;
        for (int i = siblings.size() - 1; i >= 0; i--) {
            if (containsFont(siblings.get(i), font)) {
                startIndex = i;
                break;
            }
        }

        if (startIndex < 0) {
            return containsFont(component, font) ? component.copy() : null;
        }

        if (startIndex > 0 && siblings.get(startIndex - 1).getString().isBlank()) {
            startIndex--;
        }

        MutableComponent trailingSegment = Component.empty();
        for (int i = startIndex; i < siblings.size(); i++) {
            trailingSegment.append(siblings.get(i).copy());
        }

        return trailingSegment;
    }

    private static List<Component> getIdentifiableItemTooltip(
            ItemStack itemStack, WynnItem wynnItem, IdentifiableItemProperty itemInfo) {
        ItemStatInfoFeature feature = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);

        if (shouldKeepOriginalGearTooltip(itemStack, itemInfo)) {
            return List.of();
        }

        TooltipBuilder builder = getIdentifiableTooltipBuilder(itemStack, wynnItem, itemInfo);
        if (builder == null) return null;

        TooltipIdentificationDecorator identificationDecorator =
                feature.identificationDecorations.get() ? feature.getIdentificationDecorator() : null;
        TooltipWeightDecorator weightDecorator =
                feature.itemWeights.get() != ItemWeightSource.NONE ? feature.getWeightDecorator() : null;
        TooltipStyle currentIdentificationStyle = new TooltipStyle(
                feature.identificationsOrdering.get(),
                feature.groupIdentifications.get(),
                feature.showBestValueLastAlways.get(),
                feature.showStars.get(),
                false);
        LinkedList<Component> tooltips = new LinkedList<>(builder.getTooltipLines(
                Models.Character.getClassType(),
                currentIdentificationStyle,
                identificationDecorator,
                feature.itemWeights.get(),
                weightDecorator));
        return tooltips;
    }

    private static TooltipBuilder getIdentifiableTooltipBuilder(
            ItemStack itemStack, WynnItem wynnItem, IdentifiableItemProperty itemInfo) {
        if (itemStack instanceof FakeItemStack fakeItemStack) {
            if (fakeItemStack.shouldUseBackingTooltip()) {
                return Handlers.Tooltip.buildFromItemStack(
                        fakeItemStack.getBackingItemStack(), itemInfo, false, true, fakeItemStack.getSource());
            }

            if (itemInfo.getItemInfo() instanceof GearInfo) {
                return Handlers.Tooltip.buildNew(itemInfo, false, true, fakeItemStack.getSource());
            }

            return wynnItem.getData()
                    .getOrCalculate(
                            WynnItemData.TOOLTIP_KEY,
                            () -> Handlers.Tooltip.buildNew(itemInfo, false, true, fakeItemStack.getSource()));
        }

        if (itemInfo.getItemInfo() instanceof GearInfo) {
            return Handlers.Tooltip.buildFromItemStack(itemStack, itemInfo, false, true, "");
        }

        if (itemInfo instanceof PagedItemProperty) {
            return Handlers.Tooltip.buildFromItemStack(itemStack, itemInfo, false, true, "");
        }

        return wynnItem.getData()
                .getOrCalculate(
                        WynnItemData.TOOLTIP_KEY,
                        () -> Handlers.Tooltip.buildFromItemStack(itemStack, itemInfo, false, true, ""));
    }

    private static List<Component> getCraftedItemTooltip(
            ItemStack itemStack, WynnItem wynnItem, CraftedItemProperty craftedItemProperty) {
        if (craftedItemProperty instanceof PagedItemProperty pagedItemProperty && !pagedItemProperty.isStatPage()) {
            return List.of();
        }

        TooltipBuilder builder = getCraftedTooltipBuilder(itemStack, wynnItem, craftedItemProperty);
        if (builder == null) return null;
        ItemStatInfoFeature isif = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);

        TooltipStyle currentIdentificationStyle = new TooltipStyle(
                isif.identificationsOrdering.get(),
                isif.groupIdentifications.get(),
                false,
                false,
                isif.showRollWheel.get());

        return new LinkedList<>(builder.getTooltipLines(
                Models.Character.getClassType(), currentIdentificationStyle, null, isif.itemWeights.get(), null));
    }

    private static TooltipBuilder getCraftedTooltipBuilder(
            ItemStack itemStack, WynnItem wynnItem, CraftedItemProperty craftedItemProperty) {
        if (itemStack instanceof FakeItemStack fakeItemStack) {
            return wynnItem.getData()
                    .getOrCalculate(
                            WynnItemData.TOOLTIP_KEY,
                            () -> Handlers.Tooltip.buildNew(craftedItemProperty, fakeItemStack.getSource()));
        }

        if (craftedItemProperty instanceof PagedItemProperty) {
            return Handlers.Tooltip.fromParsedItemStack(itemStack, craftedItemProperty);
        }

        return wynnItem.getData()
                .getOrCalculate(
                        WynnItemData.TOOLTIP_KEY,
                        () -> Handlers.Tooltip.fromParsedItemStack(itemStack, craftedItemProperty));
    }

    private static boolean shouldKeepOriginalGearTooltip(ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        if (itemStack instanceof FakeItemStack) {
            return false;
        }

        if (!(itemInfo instanceof PagedItemProperty pagedItemProperty)) {
            return false;
        }

        return !pagedItemProperty.isStatPage();
    }
}
