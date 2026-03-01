/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.type.StyleType;
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
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.type.IterationDecision;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public final class TooltipUtils {
    private static final Pattern GEAR_NAME_PATTERN =
            Pattern.compile("§f\uDAFF\uDFF0.\uDAFF\uDFCF§#00eb1cff.§f\uDB00\uDC02§[5bcdef](.+)");

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

        Optional<PagedItemProperty> pagedItemPropertyOpt =
                Models.Item.asWynnItemProperty(itemStack, PagedItemProperty.class);
        if (pagedItemPropertyOpt.isPresent() && !pagedItemPropertyOpt.get().isStatPage()) {
            return tooltip;
        }

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
                feature.showBestValueLastAlways.get());
        LinkedList<Component> tooltips = new LinkedList<>(builder.getTooltipLines(
                Models.Character.getClassType(),
                currentIdentificationStyle,
                identificationDecorator,
                feature.itemWeights.get(),
                weightDecorator));

        // Update name depending on overall percentage; this needs to be done every rendering
        // for rainbow/defective effects
        if (feature.overallPercentageInName.get() && itemInfo.hasOverallValue()) {
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
                false); // irrelevant for crafted items

        return new LinkedList<>(builder.getTooltipLines(
                Models.Character.getClassType(), currentIdentificationStyle, null, isif.itemWeights.get(), null));
    }

    private static void updateItemName(IdentifiableItemProperty itemInfo, LinkedList<Component> tooltips) {
        ItemStatInfoFeature isif = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);

        for (int i = 0; i < tooltips.size(); i++) {
            Component component = tooltips.get(i);
            StyledText styledText = StyledText.fromComponent(component);

            Matcher nameMatcher = styledText.getMatcher(GEAR_NAME_PATTERN);
            if (!nameMatcher.matches()) continue;

            String itemName = nameMatcher.group(1);

            StyledText updatedItemName = styledText.iterate((part, changes) -> {
                if (part.getString(null, StyleType.NONE).equals(itemName)) {
                    PartStyle partStyle = part.getPartStyle();
                    StyledTextPart newPart;

                    if (isif.perfect.get() && itemInfo.isPerfect()) {
                        changes.remove(part);
                        newPart = new StyledTextPart(
                                "Perfect " + itemName,
                                partStyle
                                        .getStyle()
                                        .withColor(CommonColors.RAINBOW.asInt())
                                        .withBold(true),
                                null,
                                Style.EMPTY);
                    } else if (isif.defective.get() && itemInfo.isDefective()) {
                        changes.remove(part);
                        newPart = new StyledTextPart(
                                "Defective " + itemName,
                                partStyle
                                        .getStyle()
                                        .withColor(ChatFormatting.DARK_RED)
                                        .withBold(true),
                                null,
                                Style.EMPTY);
                    } else {
                        newPart = new StyledTextPart(
                                " ["
                                        + new BigDecimal(itemInfo.getOverallPercentage())
                                                .setScale(isif.decimalPlaces.get(), RoundingMode.DOWN)
                                                .toPlainString()
                                        + "%]",
                                partStyle
                                        .getStyle()
                                        .withColor(ColorScaleUtils.getPercentageColor(
                                                        isif.getColorMap(),
                                                        itemInfo.getOverallPercentage(),
                                                        isif.colorLerp.get())
                                                .asInt()),
                                null,
                                Style.EMPTY);
                    }

                    changes.add(newPart);

                    return IterationDecision.BREAK;
                }

                return IterationDecision.CONTINUE;
            });

            tooltips.set(i, updatedItemName.getComponent());
            return;
        }
    }
}
