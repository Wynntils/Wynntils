/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.TooltipMarkers;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.DividerComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearFooterComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearItemWeightsComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearParsedTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearTooltipAlignmentComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.RerollBannerComponent;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public final class GearTooltipComponent extends IdentifiableTooltipComponent<GearInfo, GearInstance> {
    private final GearHeaderComponent headerComponent = new GearHeaderComponent();
    private final GearFooterComponent footerComponent = new GearFooterComponent();
    private final DividerComponent dividerComponent = new DividerComponent();
    private final RerollBannerComponent rerollBannerComponent = new RerollBannerComponent();
    private final GearItemWeightsComponent itemWeightsComponent = new GearItemWeightsComponent();
    private final GearParsedTooltipComponent parsedTooltipComponent = new GearParsedTooltipComponent();

    @Override
    public TooltipParts buildTooltipParts(
            ItemStack itemStack,
            IdentifiableItemProperty<GearInfo, GearInstance> itemProperty,
            boolean hideUnidentified,
            boolean showItemType) {
        TooltipParts parsedParts = parsedTooltipComponent.buildTooltipParts(itemStack, itemProperty);
        if (parsedParts != null) {
            return parsedParts;
        }

        GearInfo gearInfo = itemProperty.getItemInfo();
        GearInstance gearInstance = itemProperty.getItemInstance().orElse(null);
        GearInstance headerInstance = gearInstance != null ? gearInstance : createSyntheticHeaderInstance(itemProperty);
        return new TooltipParts(
                buildHeaderTooltip(gearInfo, headerInstance, hideUnidentified),
                buildFooterTooltip(gearInfo, gearInstance, showItemType));
    }

    @Override
    public List<Component> buildHeaderTooltip(GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        return headerComponent.buildHeaderTooltip(gearInfo, gearInstance, hideUnidentified);
    }

    @Override
    public List<Component> buildFooterTooltip(GearInfo gearInfo, GearInstance gearInstance, boolean showItemType) {
        return footerComponent.buildFooterTooltip(gearInfo, gearInstance, showItemType);
    }

    @Override
    public List<Component> buildWeightedHeaderTooltip(
            List<Component> originalHeader,
            IdentifiableItemProperty<GearInfo, GearInstance> itemProperty,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator,
            TooltipStyle style) {
        return itemWeightsComponent.buildWeightedHeader(originalHeader, itemProperty, weightSource, weightDecorator);
    }

    @Override
    public List<Component> finalizeTooltipLines(
            List<Component> tooltip, int targetWidth, GearInfo gearInfo, GearInstance gearInstance) {
        List<Component> finalized = new ArrayList<>(tooltip.size());
        for (Component line : tooltip) {
            TooltipMarkers marker = TooltipMarkers.fromToken(line.getStyle().getInsertion());
            if (marker == TooltipMarkers.SECTION_DIVIDER) {
                finalized.add(mark(dividerComponent.buildDivider(gearInfo.tier()), marker));
            } else if (marker == TooltipMarkers.IDENTIFICATION_DIVIDER) {
                finalized.add(mark(dividerComponent.buildDivider(gearInfo.tier()), marker));
            } else if (marker == TooltipMarkers.REROLL_BANNER) {
                finalized.add(mark(rerollBannerComponent.buildRerollBanner(gearInfo.tier(), gearInstance), marker));
            } else {
                finalized.add(line);
            }
        }

        GearTooltipAlignmentComponent.realignMarkedTooltipLines(finalized);
        return finalized;
    }

    private static Component mark(Component line, TooltipMarkers marker) {
        MutableComponent marked = line.copy();
        marked.setStyle(marked.getStyle().withInsertion(marker.token()));
        return marked;
    }

    private static GearInstance createSyntheticHeaderInstance(
            IdentifiableItemProperty<GearInfo, GearInstance> itemProperty) {
        if (!(itemProperty instanceof ShinyItemProperty shinyItemProperty)
                || shinyItemProperty.getShinyStat().isEmpty()) {
            return null;
        }

        return new GearInstance(
                List.of(), List.of(), 0, Optional.empty(), shinyItemProperty.getShinyStat(), false, Optional.empty());
    }
}
