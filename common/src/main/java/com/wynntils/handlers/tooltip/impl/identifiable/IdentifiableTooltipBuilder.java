/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.services.itemweight.type.ItemWeighting;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

/**
 * A builder for identifiable item tooltips.
 * @param <T> The type of the gear info
 * @param <U> The type of the gear instance
 */
public final class IdentifiableTooltipBuilder<T, U> extends TooltipBuilder {
    private final IdentifiableItemProperty<T, U> itemInfo;

    private IdentifiableTooltipBuilder(
            IdentifiableItemProperty<T, U> itemInfo, List<Component> header, List<Component> footer, String source) {
        super(header, footer, source);
        this.itemInfo = itemInfo;
    }

    private IdentifiableTooltipBuilder(
            IdentifiableItemProperty<T, U> itemInfo, List<Component> header, List<Component> footer) {
        this(itemInfo, header, footer, "");
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

        List<Component> header = tooltipComponent.buildHeaderTooltip(itemInfo, itemInstance, hideUnidentified);
        List<Component> footer = tooltipComponent.buildFooterTooltip(itemInfo, itemInstance, showItemType);
        return new IdentifiableTooltipBuilder(identifiableItem, header, footer, source);
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
    protected List<Component> getWeightedHeaderLines(List<Component> originalHeader, TooltipStyle style) {
        ItemWeightSource weightSource = style.weightSource();

        // Only gear will have weightings
        if (weightSource == ItemWeightSource.NONE
                || !itemInfo.hasOverallValue()
                || !(itemInfo.getItemInfo() instanceof GearInfo gearInfo)) {
            return originalHeader;
        }

        List<ItemWeighting> noriWeightings =
                Services.ItemWeight.getItemWeighting(gearInfo.name(), ItemWeightSource.NORI);
        List<ItemWeighting> wynnpoolWeightings =
                Services.ItemWeight.getItemWeighting(gearInfo.name(), ItemWeightSource.WYNNPOOL);

        boolean addNori = (weightSource == ItemWeightSource.NORI || weightSource == ItemWeightSource.ALL)
                && !noriWeightings.isEmpty();
        boolean addWynnpool = (weightSource == ItemWeightSource.WYNNPOOL || weightSource == ItemWeightSource.ALL)
                && !wynnpoolWeightings.isEmpty();

        ItemStatInfoFeature isif = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);
        int currentIndex = 1;

        List<Component> weightedHeader = new ArrayList<>(originalHeader);

        if (addNori) {
            weightedHeader.add(currentIndex, Services.ItemWeight.NORI_HEADER);
            currentIndex++;
            currentIndex = addWeightingLine(weightedHeader, noriWeightings, currentIndex, isif);
        }

        if (addWynnpool) {
            weightedHeader.add(currentIndex, Services.ItemWeight.WYNNPOOL_HEADER);
            currentIndex++;
            currentIndex = addWeightingLine(weightedHeader, wynnpoolWeightings, currentIndex, isif);
        }

        // We only need to add an empty line for weapons if any weightings were added, otherwise there will be extra
        // empty lines
        if ((addNori || addWynnpool) && gearInfo.type().isWeapon()) {
            weightedHeader.add(currentIndex, Component.empty());
        }

        return weightedHeader;
    }

    private int addWeightingLine(
            List<Component> originalHeader,
            List<ItemWeighting> weightings,
            int currentIndex,
            ItemStatInfoFeature isif) {
        for (ItemWeighting weighting : weightings) {
            MutableComponent weightingComponent = Component.literal(" - ")
                    .append(Component.literal(weighting.weightName() + " Scale"))
                    .withStyle(ChatFormatting.GRAY);

            float percentage = Services.ItemWeight.calculateWeighting(weighting, itemInfo);
            weightingComponent.append(ColorScaleUtils.getPercentageTextComponent(
                    isif.getColorMap(), percentage, isif.colorLerp.get(), isif.decimalPlaces.get()));

            originalHeader.add(currentIndex, weightingComponent);
            currentIndex++;
        }

        return currentIndex;
    }

    @Override
    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator) {
        return TooltipIdentifications.buildTooltip(itemInfo, currentClass, decorator, style);
    }
}
