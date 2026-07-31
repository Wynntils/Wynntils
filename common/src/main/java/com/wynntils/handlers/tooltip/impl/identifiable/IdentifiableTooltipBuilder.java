/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearItemWeightsComponent;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * A builder for identifiable item tooltips.
 * @param <T> The type of the gear info
 * @param <U> The type of the gear instance
 */
public final class IdentifiableTooltipBuilder<T, U> extends TooltipBuilder {
    private static final GearItemWeightsComponent DEFAULT_GEAR_ITEM_WEIGHTS_COMPONENT = new GearItemWeightsComponent();

    private final IdentifiableItemProperty<T, U> itemInfo;
    private final IdentifiableTooltipComponent<T, U> tooltipComponent;
    private final T syntheticItemInfo;
    private final U syntheticItemInstance;

    private IdentifiableTooltipBuilder(
            IdentifiableItemProperty<T, U> itemInfo,
            List<Component> header,
            List<Component> footer,
            String source,
            IdentifiableTooltipComponent<T, U> tooltipComponent,
            T syntheticItemInfo,
            U syntheticItemInstance) {
        super(header, footer, source);
        this.itemInfo = itemInfo;
        this.tooltipComponent = tooltipComponent;
        this.syntheticItemInfo = syntheticItemInfo;
        this.syntheticItemInstance = syntheticItemInstance;
    }

    private IdentifiableTooltipBuilder(
            IdentifiableItemProperty<T, U> itemInfo, List<Component> header, List<Component> footer) {
        this(itemInfo, header, footer, "", null, null, null);
    }

    /**
     * Creates a tooltip builder that provides a synthetic header and footer
     */
    public static <T, U> IdentifiableTooltipBuilder<T, U> buildNewItem(
            IdentifiableItemProperty<T, U> identifiableItem,
            IdentifiableTooltipComponent<T, U> tooltipComponent,
            boolean hideUnidentified,
            boolean showItemType,
            String source) {
        T itemInfo = identifiableItem.getItemInfo();
        U itemInstance = identifiableItem.getItemInstance().orElse(null);
        U headerItemInstance =
                itemInstance != null ? itemInstance : createSyntheticHeaderInstance(itemInfo, identifiableItem);

        List<Component> header = tooltipComponent.buildHeaderTooltip(itemInfo, headerItemInstance, hideUnidentified);
        List<Component> footer = tooltipComponent.buildFooterTooltip(itemInfo, itemInstance, showItemType);
        return new IdentifiableTooltipBuilder(
                identifiableItem, header, footer, source, tooltipComponent, itemInfo, itemInstance);
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> buildFromItemStack(
            ItemStack itemStack,
            IdentifiableItemProperty<T, U> identifiableItem,
            IdentifiableTooltipComponent<T, U> tooltipComponent,
            boolean hideUnidentified,
            boolean showItemType,
            String source) {
        T itemInfo = identifiableItem.getItemInfo();
        U itemInstance = identifiableItem.getItemInstance().orElse(null);

        IdentifiableTooltipComponent.TooltipParts tooltipParts =
                tooltipComponent.buildTooltipParts(itemStack, identifiableItem, hideUnidentified, showItemType);
        if (tooltipParts != null) {
            return new IdentifiableTooltipBuilder(
                    identifiableItem,
                    tooltipParts.header(),
                    tooltipParts.footer(),
                    source,
                    tooltipComponent,
                    itemInfo,
                    itemInstance);
        }

        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);
        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        return new IdentifiableTooltipBuilder(
                identifiableItem, splitLore.a(), splitLore.b(), source, tooltipComponent, itemInfo, itemInstance);
    }

    @SuppressWarnings("unchecked")
    private static <T, U> U createSyntheticHeaderInstance(T itemInfo, IdentifiableItemProperty<T, U> identifiableItem) {
        if (!(itemInfo instanceof GearInfo)
                || !(identifiableItem instanceof ShinyItemProperty shinyItemProperty)
                || shinyItemProperty.getShinyStat().isEmpty()) {
            return null;
        }

        return (U) new GearInstance(
                List.of(), List.of(), 0, Optional.empty(), shinyItemProperty.getShinyStat(), false, Optional.empty());
    }

    /**
     * Creates a tooltip builder that parses the header and footer from an existing tooltip
     */
    public static IdentifiableTooltipBuilder fromParsedItemStack(
            ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        List<Component> header = splitLore.a();
        List<Component> footer = splitLore.b();

        return new IdentifiableTooltipBuilder(itemInfo, header, footer);
    }

    @Override
    protected List<Component> getWeightedHeaderLines(
            List<Component> originalHeader,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator,
            TooltipStyle style) {
        if (tooltipComponent != null) {
            return tooltipComponent.buildWeightedHeaderTooltip(
                    originalHeader, itemInfo, weightSource, weightDecorator, style);
        }

        if (!(itemInfo.getItemInfo() instanceof GearInfo)) {
            return originalHeader;
        }

        @SuppressWarnings("unchecked")
        IdentifiableItemProperty<GearInfo, GearInstance> gearItemProperty =
                (IdentifiableItemProperty<GearInfo, GearInstance>) itemInfo;
        return DEFAULT_GEAR_ITEM_WEIGHTS_COMPONENT.buildWeightedHeader(
                originalHeader, gearItemProperty, weightSource, weightDecorator);
    }

    @Override
    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator, int targetWidth) {
        if (itemInfo instanceof PagedItemProperty pagedItemProperty && !pagedItemProperty.isStatPage()) {
            return List.of();
        }

        return TooltipIdentifications.buildTooltip(itemInfo, currentClass, decorator, style, targetWidth);
    }

    @Override
    protected List<Component> postProcessTooltipLines(List<Component> tooltip, int targetWidth) {
        if (tooltipComponent == null) {
            return tooltip;
        }

        return tooltipComponent.finalizeTooltipLines(tooltip, targetWidth, syntheticItemInfo, syntheticItemInstance);
    }
}
