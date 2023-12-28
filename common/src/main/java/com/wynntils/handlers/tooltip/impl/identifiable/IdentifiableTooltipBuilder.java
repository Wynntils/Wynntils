/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * A builder for identifiable item tooltips.
 * @param <T> The type of the gear info
 * @param <U> The type of the gear instance
 */
public final class IdentifiableTooltipBuilder<T, U> extends TooltipBuilder {
    private final IdentifiableItemProperty<T, U> itemInfo;

    private IdentifiableTooltipBuilder(
            IdentifiableItemProperty<T, U> itemInfo, List<Component> header, List<Component> footer) {
        super(header, footer);
        this.itemInfo = itemInfo;
    }

    /**
     * Creates a tooltip builder that provides a synthetic header and footer
     */
    public static <T, U> IdentifiableTooltipBuilder<T, U> buildNewItem(
            IdentifiableItemProperty<T, U> identifiableItem,
            IdentifiableTooltipComponent<T, U> tooltipComponent,
            boolean hideUnidentified) {
        T itemInfo = identifiableItem.getItemInfo();
        U itemInstance = identifiableItem.getItemInstance().orElse(null);

        List<Component> header = tooltipComponent.buildHeaderTooltip(itemInfo, itemInstance, hideUnidentified);
        List<Component> footer = tooltipComponent.buildFooterTooltip(itemInfo, itemInstance);
        return new IdentifiableTooltipBuilder(identifiableItem, header, footer);
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
    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator) {
        return TooltipIdentifications.buildTooltip(itemInfo, currentClass, decorator, style);
    }
}
