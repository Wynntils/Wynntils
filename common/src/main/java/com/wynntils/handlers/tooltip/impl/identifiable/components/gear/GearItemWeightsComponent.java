/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components.gear;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.TooltipMarkers;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.services.itemweight.type.ItemWeighting;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class GearItemWeightsComponent {
    private static final CustomColor NORI_SOURCE_COLOR = CustomColor.fromInt(0x67CCF5);
    private static final CustomColor WYNNPOOL_SOURCE_COLOR = CustomColor.fromInt(0xFFC457);
    private static final FontDescription DIVIDER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/divider"));

    public List<Component> buildWeightedHeader(
            List<Component> originalHeader,
            IdentifiableItemProperty<GearInfo, GearInstance> itemProperty,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator) {
        if (itemProperty.getItemInstance().isEmpty()) {
            return originalHeader;
        }

        if (itemProperty instanceof PagedItemProperty pagedItemProperty && !pagedItemProperty.isStatPage()) {
            return originalHeader;
        }

        if (weightSource == ItemWeightSource.NONE) {
            return originalHeader;
        }

        GearInfo gearInfo = itemProperty.getItemInfo();
        List<ItemWeighting> noriWeightings =
                Services.ItemWeight.getItemWeighting(gearInfo.name(), ItemWeightSource.NORI);
        List<ItemWeighting> wynnpoolWeightings =
                Services.ItemWeight.getItemWeighting(gearInfo.name(), ItemWeightSource.WYNNPOOL);

        boolean addNori = (weightSource == ItemWeightSource.NORI || weightSource == ItemWeightSource.ALL)
                && !noriWeightings.isEmpty();
        boolean addWynnpool = (weightSource == ItemWeightSource.WYNNPOOL || weightSource == ItemWeightSource.ALL)
                && !wynnpoolWeightings.isEmpty();
        if (!addNori && !addWynnpool) {
            return originalHeader;
        }

        List<Component> weightedHeader = new ArrayList<>(originalHeader);
        int currentIndex = findWeightInsertIndex(weightedHeader);
        boolean addedAny = false;
        weightedHeader.add(currentIndex, markSectionDivider(Component.empty()));
        currentIndex++;

        if (addNori) {
            weightedHeader.add(currentIndex, buildWeightSourceHeader(ItemWeightSource.NORI));
            currentIndex++;
            currentIndex =
                    addWeightingLines(weightedHeader, noriWeightings, weightDecorator, itemProperty, currentIndex);
            addedAny = true;

            if (!weightedHeader.get(currentIndex - 1).equals(Component.empty())) {
                weightedHeader.add(currentIndex, Component.empty());
                currentIndex++;
            }
        }

        if (addWynnpool) {
            weightedHeader.add(currentIndex, buildWeightSourceHeader(ItemWeightSource.WYNNPOOL));
            currentIndex++;
            currentIndex =
                    addWeightingLines(weightedHeader, wynnpoolWeightings, weightDecorator, itemProperty, currentIndex);
            addedAny = true;

            if (!weightedHeader.get(currentIndex - 1).equals(Component.empty())) {
                weightedHeader.add(currentIndex, Component.empty());
                currentIndex++;
            }
        }

        if (addedAny
                && currentIndex > 0
                && currentIndex <= weightedHeader.size()
                && !weightedHeader.get(currentIndex - 1).equals(Component.empty())) {
            weightedHeader.add(currentIndex, Component.empty());
        }

        return weightedHeader;
    }

    private static int findWeightInsertIndex(List<Component> header) {
        for (int i = 0; i < header.size(); i++) {
            Component line = header.get(i);
            if (TooltipMarkers.SECTION_DIVIDER.matches(line.getStyle().getInsertion())
                    || containsFont(line, DIVIDER_FONT)) {
                return i;
            }
        }

        return Math.min(1, header.size());
    }

    private static MutableComponent markSectionDivider(MutableComponent line) {
        line.setStyle(line.getStyle().withInsertion(TooltipMarkers.SECTION_DIVIDER.token()));
        return line;
    }

    private static Component buildWeightSourceHeader(ItemWeightSource source) {
        return switch (source) {
            case NORI -> BannerBoxFont.buildMessage("nori", NORI_SOURCE_COLOR, CommonColors.BLACK, "");
            case WYNNPOOL -> BannerBoxFont.buildMessage("wynnpool", WYNNPOOL_SOURCE_COLOR, CommonColors.BLACK, "");
            default -> Component.empty();
        };
    }

    public static MutableComponent buildRightAlignedWeightLine(Component left, Component right) {
        MutableComponent line =
                Component.empty().withStyle(Style.EMPTY.withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT));
        line.append(left.copy());
        line.append(trimLeadingSpace(right));
        line.setStyle(line.getStyle().withInsertion(TooltipMarkers.ALIGN_RIGHT.token()));
        return line;
    }

    public static MutableComponent withLanguageFont(MutableComponent component) {
        return Component.empty()
                .withStyle(Style.EMPTY.withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT))
                .append(component);
    }

    private static int addWeightingLines(
            List<Component> originalHeader,
            List<ItemWeighting> weightings,
            TooltipWeightDecorator weightDecorator,
            IdentifiableItemProperty<GearInfo, GearInstance> itemProperty,
            int currentIndex) {
        for (ItemWeighting weighting : weightings) {
            for (MutableComponent component : weightDecorator.getLines(weighting, itemProperty)) {
                originalHeader.add(currentIndex, component);
                currentIndex++;
            }
        }

        return currentIndex;
    }

    private static boolean containsFont(Component component, FontDescription targetFont) {
        if (targetFont.equals(component.getStyle().getFont())) {
            return true;
        }

        for (Component sibling : component.getSiblings()) {
            if (containsFont(sibling, targetFont)) {
                return true;
            }
        }

        return false;
    }

    private static Component trimLeadingSpace(Component component) {
        if (!component.getSiblings().isEmpty()) {
            return component.copy();
        }

        String text = component.getString();
        if (!text.startsWith(" ")) {
            return component.copy();
        }

        return Component.literal(text.substring(1)).withStyle(component.getStyle());
    }
}
