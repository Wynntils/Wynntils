/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.services.itemweight.type.ItemWeighting;
import java.util.List;
import net.minecraft.network.chat.MutableComponent;

@FunctionalInterface
public interface TooltipWeightDecorator {
    List<MutableComponent> getLines(ItemWeighting weighting, IdentifiableItemProperty<?, ?> itemInfo);
}
