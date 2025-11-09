/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
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

    private static List<Component> getIdentifiableItemTooltip(
            ItemStack itemStack, WynnItem wynnItem, IdentifiableItemProperty itemInfo) {
        TooltipBuilder builder = wynnItem.getData()
                .getOrCalculate(
                        WynnItemData.TOOLTIP_KEY, () -> Handlers.Tooltip.fromParsedItemStack(itemStack, itemInfo));
        if (builder == null) return null;
        ItemStatInfoFeature feature = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);

        TooltipIdentificationDecorator identificationDecorator =
                feature.identificationDecorations.get() ? feature.getIdentificationDecorator() : null;
        TooltipWeightDecorator weightDecorator =
                feature.itemWeights.get() != ItemWeightSource.NONE ? feature.getWeightDecorator() : null;
        TooltipStyle currentIdentificationStyle = new TooltipStyle(
                feature.identificationsOrdering.get(),
                feature.groupIdentifications.get(),
                feature.showBestValueLastAlways.get(),
                feature.showStars.get(),
                false // this only applies to crafted items
                );
        LinkedList<Component> tooltips = new LinkedList<>(builder.getTooltipLines(
                Models.Character.getClassType(),
                currentIdentificationStyle,
                identificationDecorator,
                feature.itemWeights.get(),
                weightDecorator));

        // Update name depending on overall percentage; this needs to be done every rendering
        // for rainbow/defective effects
        boolean isShiny = (wynnItem instanceof ShinyItemProperty shinyItemProperty
                && shinyItemProperty.getShinyStat().isPresent());
        if (feature.overallPercentageInName.get() && itemInfo.hasOverallValue()) {
            updateItemName(itemInfo, isShiny, tooltips);
        }
        return tooltips;
    }

    private static List<Component> getCraftedItemTooltip(
            ItemStack itemStack, WynnItem wynnItem, CraftedItemProperty craftedItemProperty) {
        TooltipBuilder builder = wynnItem.getData()
                .getOrCalculate(
                        WynnItemData.TOOLTIP_KEY,
                        () -> Handlers.Tooltip.fromParsedItemStack(itemStack, craftedItemProperty));
        if (builder == null) return null;
        ItemStatInfoFeature isif = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);

        TooltipStyle currentIdentificationStyle = new TooltipStyle(
                isif.identificationsOrdering.get(),
                isif.groupIdentifications.get(),
                false, // irrelevant for crafted items
                false, // irrelevant for crafted items
                isif.showMaxValues.get());

        return new LinkedList<>(builder.getTooltipLines(
                Models.Character.getClassType(), currentIdentificationStyle, null, isif.itemWeights.get(), null));
    }

    private static void updateItemName(IdentifiableItemProperty itemInfo, boolean isShiny, Deque<Component> tooltips) {
        MutableComponent name = Component.empty();
        String itemName = itemInfo.getName();
        ItemStatInfoFeature isif = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);

        if (isShiny) {
            name = Component.literal("⬡ ");
            itemName = "Shiny " + itemName;
        }

        if (isif.perfect.get() && itemInfo.isPerfect()) {
            name.append(ComponentUtils.makeRainbowStyle("Perfect " + itemName, true));
        } else if (isif.defective.get() && itemInfo.isDefective()) {
            name.append(ComponentUtils.makeObfuscated(
                    "Defective " + itemName, isif.obfuscationChanceStart.get(), isif.obfuscationChanceEnd.get()));
        } else {
            // This already contains the ⬡ if it is a shiny item so we don't append the line
            name = tooltips.getFirst().copy();
            name.append(ColorScaleUtils.getPercentageTextComponent(
                    isif.getColorMap(),
                    itemInfo.getOverallPercentage(),
                    isif.colorLerp.get(),
                    isif.decimalPlaces.get()));
        }
        tooltips.removeFirst();
        tooltips.addFirst(name);
    }
}
