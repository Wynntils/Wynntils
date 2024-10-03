/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.features.tooltips.ItemStatInfoFeature.IdentificationDecorator;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
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
                + lines.stream().mapToInt(ClientTooltipComponent::getHeight).sum();
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
        ItemStatInfoFeature isif = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);

        IdentificationDecorator decorator =
                isif.identificationDecorations.get() ? isif.new IdentificationDecorator() : null;
        TooltipStyle currentIdentificationStyle = new TooltipStyle(
                isif.identificationsOrdering.get(),
                isif.groupIdentifications.get(),
                isif.showBestValueLastAlways.get(),
                isif.showStars.get(),
                false // this only applies to crafted items
                );
        LinkedList<Component> tooltips = new LinkedList<>(
                builder.getTooltipLines(Models.Character.getClassType(), currentIdentificationStyle, decorator));

        // Update name depending on overall percentage; this needs to be done every rendering
        // for rainbow/defective effects
        if (isif.overallPercentageInName.get() && itemInfo.hasOverallValue()) {
            updateItemName(itemInfo, tooltips);
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

        return new LinkedList<>(
                builder.getTooltipLines(Models.Character.getClassType(), currentIdentificationStyle, null));
    }

    private static void updateItemName(IdentifiableItemProperty itemInfo, Deque<Component> tooltips) {
        MutableComponent name;
        ItemStatInfoFeature isif = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);

        if (isif.perfect.get() && itemInfo.isPerfect()) {
            name = ComponentUtils.makeRainbowStyle("Perfect " + itemInfo.getName());
        } else if (isif.defective.get() && itemInfo.isDefective()) {
            name = ComponentUtils.makeObfuscated(
                    "Defective " + itemInfo.getName(),
                    isif.obfuscationChanceStart.get(),
                    isif.obfuscationChanceEnd.get());
        } else {
            name = tooltips.getFirst().copy();
            name.append(ColorScaleUtils.getPercentageTextComponent(
                    itemInfo.getOverallPercentage(), isif.colorLerp.get(), isif.decimalPlaces.get()));
        }
        tooltips.removeFirst();
        tooltips.addFirst(name);
    }
}
