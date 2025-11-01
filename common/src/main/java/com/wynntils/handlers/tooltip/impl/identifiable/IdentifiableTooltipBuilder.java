/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.core.components.Services;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.services.itemweight.type.ItemWeighting;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
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
    protected List<Component> getWeightedHeaderLines(
            List<Component> originalHeader,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator,
            TooltipStyle style) {
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

        int currentIndex = 1;

        List<Component> weightedHeader = new ArrayList<>(originalHeader);

        if (addNori) {
            weightedHeader.add(currentIndex, Services.ItemWeight.NORI_HEADER);
            currentIndex++;
            currentIndex = addWeightingLines(weightedHeader, noriWeightings, weightDecorator, currentIndex);

            if (!weightedHeader.get(currentIndex - 1).equals(Component.empty())) {
                weightedHeader.add(currentIndex, Component.empty());
                currentIndex++;
            }
        }

        if (addWynnpool) {
            weightedHeader.add(currentIndex, Services.ItemWeight.WYNNPOOL_HEADER);
            currentIndex++;
            currentIndex = addWeightingLines(weightedHeader, wynnpoolWeightings, weightDecorator, currentIndex);

            if (!weightedHeader.get(currentIndex - 1).equals(Component.empty())) {
                weightedHeader.add(currentIndex, Component.empty());
                currentIndex++;
            }
        }

        if (addNori || addWynnpool) {
            if (gearInfo.type().isWeapon() && weightedHeader.get(currentIndex).equals(Component.empty())) {
                weightedHeader.remove(currentIndex);
            } else if (gearInfo.type().isWeapon()
                    && !weightedHeader.get(currentIndex - 1).equals(Component.empty())) {
                weightedHeader.add(currentIndex, Component.empty());
            } else if (!gearInfo.type().isWeapon()
                    && weightedHeader.get(currentIndex - 1).equals(Component.empty())) {
                weightedHeader.remove(currentIndex - 1);
            }
        }

        return weightedHeader;
    }

    private int addWeightingLines(
            List<Component> originalHeader,
            List<ItemWeighting> weightings,
            TooltipWeightDecorator weightDecorator,
            int currentIndex) {
        for (ItemWeighting weighting : weightings) {
            for (MutableComponent component : weightDecorator.getLines(weighting, itemInfo)) {
                originalHeader.add(currentIndex, component);
                currentIndex++;
            }
        }

        return currentIndex;
    }

    @Override
    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator) {
        return TooltipIdentifications.buildTooltip(itemInfo, currentClass, decorator, style);
    }
}
